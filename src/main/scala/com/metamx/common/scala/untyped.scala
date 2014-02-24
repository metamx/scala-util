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

package com.metamx.common.scala

import com.metamx.common.scala.Predef._
import java.{util => ju, lang => jl}
import scala.collection.immutable
import scala.collection.JavaConverters._
import scala.{collection => _collection}

// Casts and conversions from "untyped" data, like what we get from parsing json, yaml, etc.
// Shamelessly inspired by python.
object untyped {

  // Casts: null input throws NPE

  def bool   (x: Any, onNull: => Boolean = throwNPE): Boolean = (x mapNull onNull).asInstanceOf[jl.Boolean].booleanValue
  def int    (x: Any, onNull: => Int     = throwNPE): Int     = (x mapNull onNull).asInstanceOf[jl.Number].intValue
  def long   (x: Any, onNull: => Long    = throwNPE): Long    = (x mapNull onNull).asInstanceOf[jl.Number].longValue
  def float  (x: Any, onNull: => Float   = throwNPE): Float   = (x mapNull onNull).asInstanceOf[jl.Number].floatValue
  def double (x: Any, onNull: => Double  = throwNPE): Double  = (x mapNull onNull).asInstanceOf[jl.Number].doubleValue

  def str(x: Any, onNull: => String = throwNPE): String = {
    tryCasts(x mapNull onNull)(
      _.asInstanceOf[String],
      x => new String(x.asInstanceOf[Array[Byte]])
    )
  }

  // TODO Tests
  def dict(x: Any, onNull: => Dict = throwNPE): Dict = {
    tryCasts(x mapNull onNull)(
      _.asInstanceOf[Dict],
      _.asInstanceOf[ju.Map[String,Any]].asScala.toMap,
      kv => Dict(kv.asInstanceOf[(String, Any)]),
      list(_).asInstanceOf[_collection.Seq[(String,Any)]].toMap
    )
  }

  // TODO Tests
  def list(x: Any, onNull: => UList = throwNPE): UList = {
    tryCasts(x mapNull onNull)(
      _.asInstanceOf[UList],
      _.asInstanceOf[ju.List[Any]].asScala.toList,
      _.asInstanceOf[Array[Any]].toList,
      _.asInstanceOf[TraversableOnce[Any]].toList
    )
  }

  type Dict                         = immutable.Map[String, Any]
  def  Dict(elems: (String, Any)*)  = immutable.Map.apply[String, Any](elems : _*)

  type UList                        = immutable.Seq[Any]
  def  UList(elems: (String, Any)*) = immutable.Seq.apply[Any](elems : _*)

  // TODO Tests
  def tryCasts[X,Y](x: X)(f: X => Y, fs: (X => Y)*): Y = {
    for (g <- f +: fs) {
      try {
        return g(x)
      } catch {
        case e: ClassCastException => ()
      }
    }
    throw new ClassCastException("No casts succeeded for: %s" format x.asInstanceOf[AnyRef].getClass)
  }

  // Nested collections

  // Recursively normalize dict-like objects into Maps and all other collections into Seqs
  def normalize(x: Any): Any = gfold(x)(Map.empty)

  // Recursively normalize dict-like objects into ju.Maps and all other collections into ju.Lists
  def normalizeJava(x: Any): Any = gfold(x) {
    case x: Map[_,_] => x.asJava : ju.Map[_,_]
    case x: Seq[_]   => x.asJava : ju.List[_]
  }

  // Generically "fold" x as a tree of dict- and list-like objects, normalizing them to Maps and Seqs so that f is
  // guaranteed to see all dict-likes as Maps and list-likes as Seqs (e.g. no ju.Maps or Sets). Nodes on which f isn't
  // defined are mapped through identity.
  def gfold(x: Any)(_f: PartialFunction[Any, Any]): Any = {
    val f = _f orElse { case x => x } : PartialFunction[Any, Any]
    x match {

      case x: Map[_,_]                       => f(x.mapValues(gfold(_)(f)))
      case x: Seq[_]                         => f(x.map(gfold(_)(f)))

      case x: _collection.Map[_,_]           => gfold(x.toMap: Map[_,_])(f) // -> Predef.Map (= immutable.Map)
      case x: _collection.TraversableOnce[_] => gfold(x.toSeq: Seq[_])(f)   // -> Predef.Seq (= immutable.Seq)
      case x: Array[_]                       => gfold(x.toSeq)(f)           // Arrays are ill behaved, make them Seqs
      case x: ju.Map[_,_]                    => gfold(x.asScala)(f)         // Java -> scala
      case x: jl.Iterable[_]                 => gfold(x.asScala)(f)         // Java -> scala

      case x                                 => f(x)

    }
  }

  // Null

  def noNull[X](x: X): X = x mapNull throwNPE

  def throwNPE = throw new NullPointerException("Non-null input required")

}
