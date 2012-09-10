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

import com.metamx.common.Backoff
import com.metamx.common.scala.control.untilSome
import com.metamx.common.scala.option.OptionOps
import scala.reflect.ClassManifest

import scala.annotation.tailrec

object control extends Logging {

  @tailrec
  def untilSome[X](x: => Option[X]): X = x match {
    case Some(y) => y
    case None    => untilSome(x)
  }

  // This implementation isn't space safe, since Stream.flatten uses stack space proportional to the number of
  // consecutive None's [https://issues.scala-lang.org/browse/SI-153]
  //def untilSome[X](x: => Option[X]): X = Stream.continually(x).flatten.head

  def retryOnError[E <: Exception](isTransient: E => Boolean) = new {
    def apply[X](x: => X)(implicit cm: ClassManifest[E]): X = {
      withBackoff { backoff =>
        try Some(x) catch {
          case e: Exception if cm.erasure.isAssignableFrom(e.getClass) && isTransient(e.asInstanceOf[E]) =>
            log.warn(e, "Transient error, retrying after %s ms", backoff.next)
            None
        }
      }
    }
  }

  def withBackoff[X](f: Backoff => Option[X]): X = {
    val backoff = new Backoff(200, 2, 30000)
    untilSome {
      f(backoff) ifEmpty {
        backoff.sleep
      }
    }
  }

}
