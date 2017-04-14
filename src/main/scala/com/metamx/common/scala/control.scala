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

import com.github.nscala_time.time.Imports._
import com.metamx.common.Backoff
import com.metamx.common.scala.option.OptionOps
import scala.annotation.tailrec
import scala.reflect.ClassTag

object control extends Logging {

  @tailrec
  def untilSome[X](x: => Option[X]): X = x match {
    case Some(y) => y
    case None    => untilSome(x)
  }

  def retryOnError[E <: Exception](isTransient: E => Boolean) = new {
    def apply[X](x: => X)(implicit ct: ClassTag[E]) = retryOnErrors(
      (e: Exception) => ct.runtimeClass.isAssignableFrom(e.getClass) && isTransient(e.asInstanceOf[E])
    )(x)
  }

  // FIXME When used with untilPeriod, the last sleep is useless
  def retryOnErrors[X](isTransients: (Exception => Boolean)*)(x: => X): X = {
    withBackoff { backoff =>
      try Some(x) catch {
        case e: Exception if isTransients.find(_(e)).isDefined =>
          log.warn(e, "Transient error, retrying after %s ms", backoff.next)
          None
      }
    }
  }

  def withBackoff[X](f: Backoff => Option[X]): X = {
    val backoff = Backoff.standard()
    untilSome {
      f(backoff) ifEmpty {
        backoff.sleep
      }
    }
  }

  def ifException[E <: Exception](implicit ct: ClassTag[E]) = (e: Exception) =>
    ct.runtimeClass.isAssignableFrom(e.getClass)

  def ifExceptionSatisfies[E <: Exception](pred: E => Boolean)(implicit ct: ClassTag[E]) = (e: Exception) =>
    ct.runtimeClass.isAssignableFrom(e.getClass) && pred(e.asInstanceOf[E])

  class PredicateOps[A](f: A => Boolean)
  {
    def untilCount(count: Int) = {
      var n = 0
      (a: A) => if (n < count) {
        val x = f(a)
        n += 1
        x
      } else {
        false
      }
    }

    def untilPeriod(period: Period) = {
      val end = DateTime.now + period
      (a: A) => if (DateTime.now < end) f(a) else false
    }
  }

  implicit def PredicateOps[A](f: A => Boolean) = new PredicateOps(f)

}
