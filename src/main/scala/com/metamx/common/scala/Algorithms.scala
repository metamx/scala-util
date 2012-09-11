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

object Algorithms {

  // From com.metamx.druid.utils.BufferUtils.binarySearch translated to Scala
  def binarySearch[A](seq: IndexedSeq[A], minIndex: Int, maxIndex: Int, value: A)(implicit ordering: Ordering[A]): Int = {
    var _minIndex = minIndex
    var _maxIndex = maxIndex
    while (_minIndex < _maxIndex) {
      val currIndex = (_minIndex + _maxIndex - 1) >>> 1
      val currValue = seq(currIndex)
      val comparison = ordering.compare(currValue, value)
      if (comparison == 0) {
        return currIndex
      }
      if (comparison < 0) {
        _minIndex = currIndex + 1
      } else {
        _maxIndex = currIndex
      }
    }
    return -(_minIndex + 1)
  }
}
