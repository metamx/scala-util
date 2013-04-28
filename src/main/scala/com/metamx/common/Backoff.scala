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

package com.metamx.common

import _root_.scala.util.Random

class Backoff(start: Long, growth: Double, max: Long, fuzz: Double) {
  def this(start: Long, growth: Double, max: Long) = this(start, growth, max, .2) // (Java doesn't speak default args)

  var next: Long = fuzzy(start)
  def sleep {
    Thread.sleep(next)
    next = fuzzy(math.min(max, (next * growth).toInt))
  }
  def reset {
    next = start
  }

  def fuzzy(x: Long): Long = (math.max(1 + fuzz * Random.nextGaussian, 0) * x).toLong

}
