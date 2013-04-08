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

/**
 * A late val is a single-assignment val that can be assigned after definition. Subsequent
 * assignments raise an error, and dereferencing a late val before assignment also raises an error.
 *
 *   val x = new LateVal[Int]
 *   ...
 *   x.deref     // BAD
 *   x.assign(3) // Good
 *   x.deref     // Good
 *   x.assign(4) // BAD
 *
 * A LateVal[X] can be used as an X, via the implicit conversion LateVal. Moreover, the method
 * names LateVal.assign and LateVal.deref are chosen to minimize shadowing whatever methods will be
 * available on X; in particular, LateVal.set and LateVal.get would shadow methods for many common
 * choices of X.
 *
 *   val x = new LateVal[Int]
 *   x.assign(3)
 *   x + 1
 *
 *   val x = new LateVal[Map[Int, String]]
 *   x.assign(Map(1 -&gt; "one"))
 *   x.get(1)
 *
 */
object LateVal {

  class LateVal[X] {

    val monitor = new AnyRef
    @volatile private var v: Option[X] = None

    def assign(x: X) {
      monitor synchronized {
        if (v == None) {
          v = Some(x)
        } else {
          throw new IllegalStateException("LateVal(%s) already defined: assign(%s)" format (v.get, x))
        }
      }
    }

    def deref: X = v getOrElse {
      throw new IllegalArgumentException("Undefined LateVal")
    }

    def derefOption: Option[X] = v

  }
  implicit def LateVal[X](x: LateVal[X]): X = x.deref

}
