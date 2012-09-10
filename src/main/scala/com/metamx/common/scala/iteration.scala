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

object iteration {

  implicit def toTimes(n: Int) = new {
    def times(f: => Any) {
      var i = n
      while (i > 0) {
        f
        i -= 1
      }
    }
  }

  class IterablePairOps[K,V](self: Iterable[(K,V)]) {
    def toManyMap: Map[K, Iterable[V]] =
      self groupBy (_._1) mapValues (_.map(_._2))
  }
  implicit def IterablePairOps[K,V](xs: Iterable[(K,V)]) = new IterablePairOps(xs)

  // Need higher kinds to do this right
  // class IterableLikeOps[X, Y, Repr](xs: IterableLike[(X,Y), Repr]) {
  //   def toManyMap: Map[X, Repr[Y]] = ...
  // }

}
