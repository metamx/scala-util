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

import com.metamx.common.scala.Predef._

// TODO Extract not-mysql-specific stuff from createIsTransient, push up to DB. Must:
//  - be easy to combine: `new DB(config) with MySQLErrors' or `new DB(config, mySqlErrors)'
//  - not be error prone
//  - support recursive isTransient test

class MySQLDB(config: DBConfig) extends DB(config) {

  override def createIsTransient: Throwable => Boolean = {

    var timeoutLimit = 3

    def isTransient(e: Throwable): Boolean = e match {

      // If our query repeatedly fails to finish, then we should probably stop doing it
      case e: com.mysql.jdbc.exceptions.MySQLTimeoutException =>
        log.info("DB query timed out: timeoutLimit = %s", timeoutLimit)
        (timeoutLimit > 1) andThen {
          timeoutLimit -= 1
        }

      // Anything marked "transient"
      case e: java.sql.SQLTransientException                                       => true
      case e: com.mysql.jdbc.exceptions.MySQLTransientException                    => true

      // IO errors from jdbc look like this [are we responsible for force-closing the connection in this case?]
      case e: java.sql.SQLRecoverableException                                     => true

      // Specific errors from jdbi with no useful supertype
      case e: org.skife.jdbi.v2.exceptions.UnableToObtainConnectionException       => true
      //case e: org.skife.jdbi.v2.exceptions.UnableToCloseResourceException        => true // TODO Include this one?

      // MySQL ER_QUERY_INTERRUPTED "Query execution was interrupted" [ETL-153]
      case e: java.sql.SQLException                     if e.getErrorCode == 1317  => true

      // Unwrap nested exceptions from jdbc and jdbi
      case e: java.sql.SQLException                     if isTransient(e.getCause) => true
      case e: org.skife.jdbi.v2.exceptions.DBIException if isTransient(e.getCause) => true

      // Nothing else
      case e                                                                       => false

    }

    isTransient _

  }

  override def createTable(table: String, decls: Seq[String]) {
    execute("create table %s (%s) engine=innodb charset=utf8" format (table, decls mkString ", "))
  }
}
