/*
 * Copyright 2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.scala.db

import com.mchange.v2.c3p0.DataSources
import com.metamx.common.lifecycle.LifecycleStart
import com.metamx.common.lifecycle.LifecycleStop
import com.metamx.common.scala.collection._
import com.metamx.common.scala.exception.raises
import com.metamx.common.scala.exception._
import com.metamx.common.scala.Logging
import com.metamx.common.scala.Predef._
import java.net.URI
import javax.sql.DataSource
import org.scala_tools.time.Imports._
import org.skife.jdbi.v2.DBI
import org.skife.jdbi.v2.exceptions.CallbackFailedException
import org.skife.jdbi.v2.exceptions.StatementException
import org.skife.jdbi.v2.exceptions.TransactionFailedException
import org.skife.jdbi.v2.Handle
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.TransactionCallback
import org.skife.jdbi.v2.TransactionStatus
import org.skife.jdbi.v2.tweak.HandleCallback
import scala.collection.JavaConverters._
import scala.collection.mutable.Buffer
import scala.util.DynamicVariable

import com.metamx.common.scala.control.retryOnError
import com.metamx.common.scala.LateVal.LateVal
import com.metamx.common.scala.untyped.Dict

// TODO Extract not-mysql-specific stuff from MySQLDB.createIsTransient, provide base implementation here

abstract class DB(config: DBConfig) extends Logging {

  def createIsTransient: Throwable => Boolean

  def select(sql: String, args: Any*): IndexedSeq[Dict] = {
    inTransaction {
      log.trace("select: %s, %s", oneLineSql(sql), args)
      val query = configured(h.createQuery(sql))
      for (i <- args.indices) {
        query.bind(i, args(i).asInstanceOf[AnyRef])
      }
      val results = query.list
      IndexedSeq[Dict]() ++ results.asScala.map(_.asScala.toMap) withEffect { rows =>
        log.trace("%s rows <- %s, %s", rows.length, oneLineSql(sql), args)
      }
    }
  }

  // Streaming select, paginated by uniqueKey, which must be a selected name. Hacky, but useful. Example:
  //
  //   stream(uniqueKey="id", select="id,uri,mtime", from="files", where="uri = ? and mtime > ?", uri, mtime)
  //
  // Atomic if all uses of result Stream are enclosed within inTransaction{...}.
  def stream(uniqueKey: String, select: String, from: String, where: String, args: Any*): Stream[Dict] = {
    log.trace("stream[%s]: select [%s] from [%s] where [%s], %s", uniqueKey, select, from, where, args)
    val _fetchSize = fetchSize.value // Snapshot fetchSize: our result stream can be evaluated in many dynamic scopes
    var last = None : Option[Any]
    Stream.from(0) map { i =>
      this.select(
        "select %s from %s where (%s) and %s order by %s limit ?" format (
          select,
          from,
          if (where.nonEmpty) where else "true",
          last map (_ => "%s > ?" format uniqueKey) getOrElse "true",
          uniqueKey
        ),
        args ++ last ++ Seq(_fetchSize) : _*
      ) withEffect { rows =>
        last = rows.lastOption map (_(uniqueKey))
      }
    } takeUntil (_.length < _fetchSize) flatten
  }

  val fetchSize = new DynamicVariable[Int](config.fetchSize)

  def execute(sql: String, args: Any*): Int = {
    inTransaction {
      log.trace("execute: %s, %s", oneLineSql(sql), args)
      val query = configured(h.createStatement(sql))
      for (i <- args.indices) {
        query.bind(i, args(i).asInstanceOf[AnyRef])
      }
      query.execute
    }
  }

  // Atomic
  def batch(sql: String, argss: Iterable[Seq[Any]]): Int = {
    inTransaction {
      argss.grouped(config.batchSize).map { argss => // (Is this necessary? Does something below already chunk?)
        log.trace("batch: %s, %s", oneLineSql(sql), argss)
        val query = configured(h.prepareBatch(sql))
        for (args <- argss) {
          query.add(args.map(_.asInstanceOf[AnyRef]): _*)
        }
        query.execute.sum
      }.sum
    }
  }

  // Ensure a transaction. Reentrant. Outermost inTransaction retries on "transient" errors (using createIsTransient).
  def inTransaction[X](body: => X): X = currentTransationStatus.value match {
    case Some(_tx) => body
    case None      =>
      retryOnError(createIsTransient) {
        singleHandle {
          h.inTransaction(new TransactionCallback[X] { def inTransaction(_h: Handle, _tx: TransactionStatus) = {
            currentTransationStatus.withValue(Some(_tx)) { body }
          }}) mapException {
            case e: TransactionFailedException => e.getCause // Wrapped exceptions are anti-useful; unwrap them
          }
        }
      }
  }

  // Fix a single handle. Reentrant. Doesn't retry on transient errors.
  def singleHandle[X](body: => X): X = currentHandle.value match {
    case Some(_h) => body
    case None     =>
      dbi.withHandle(new HandleCallback[X] { def withHandle(_h: Handle) = {
        currentHandle.withValue(Some(_h)) { body }
      }}) mapException {
        case e: CallbackFailedException => e.getCause // Wrapped exceptions are anti-useful; unwrap them
      }
  }

  private[this] val currentHandle           = new DynamicVariable[Option[Handle]](None)
  private[this] val currentTransationStatus = new DynamicVariable[Option[TransactionStatus]](None)

  // Convenient, idiomatic names for currentHandle, currentTransationStatus
  def h  : Handle            = currentHandle.value.get
  def tx : TransactionStatus = currentTransationStatus.value.get

  def configured[X <: SQLStatement[X]](x: X): X = { // TODO Figure out jdbi's statement customizers
    x.setQueryTimeout(config.queryTimeout.seconds.toInt)
  }

  def oneLineSql(sql: String) = """\s*\n\s*""".r.replaceAllIn(sql, " ").trim

  @LifecycleStart
  def start {
    log.info("Starting")
    dbds assign DataSources.pooledDataSource(
      DataSources.unpooledDataSource(
        config.uri,
        config.user,
        config.password
      )
    )
    log.info("Connecting to %s", config.uri)
    dbi assign new DBI(dbds)
    schema.create
  }

  @LifecycleStop
  def stop {
    DataSources.destroy(dbds)
  }

  val dbds = new LateVal[DataSource]
  val dbi  = new LateVal[DBI]

  lazy val schema = new {

    lazy val tables = Buffer[(String, Seq[String])]() // (Maintain declaration order for creation)

    def create {
      for ((table, decls) <- tables) {
        if (exists(table)) {
          log.info("Table already exists: %s", table)
        } else {
          log.info("Creating table: %s", table)
          execute("create table %s (%s) engine=innodb charset=utf8" format (table, decls mkString ", "))
        }
      }
    }

    def exists(table: String) = !raises[StatementException] { select("select * from %s limit 0" format table) }

  }

}
