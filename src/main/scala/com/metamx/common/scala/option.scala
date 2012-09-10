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

object option {

  class OptionOps[X](x: Option[X]) {

    def ifEmpty    (f: => Any):   Option[X] = { if (x.isEmpty)   f;        x } // Like TraversableOnceOps
    def ifNonEmpty (f: => Any):   Option[X] = { if (x.nonEmpty)  f;        x } // Like TraversableOnceOps
    def ifDefined  (f: X => Any): Option[X] = { if (x.isDefined) f(x.get); x } // Not in TraversableOnceOps

    def andThen[Y](f: X => Option[Y]): Option[Y] = x flatMap f

  }
  implicit def OptionOps[X](x: Option[X]): OptionOps[X] = new OptionOps(x)

}
