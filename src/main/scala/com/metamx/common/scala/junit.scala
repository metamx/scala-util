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

import com.metamx.common.scala.exception._
import com.metamx.common.scala.Predef._

object junit {

  // Dump context `xs' to stdout if any test (i.e. assertion) in `body' fails
  def inContext[X](xs: Any*)(body: => X): X = body mapException {
    case e: AssertionError => e withEffect { _ =>
      println("In context: %s" format (xs mkString ", "))
    }
  }

  def contextually[X,Y](f: X => Y): X => Y = {
    x => inContext(x) { f(x) }
  }

}
