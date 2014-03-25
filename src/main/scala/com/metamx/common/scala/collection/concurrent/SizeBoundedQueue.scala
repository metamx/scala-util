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

package com.metamx.common.scala.collection.concurrent

import java.util.concurrent.atomic.AtomicLong
import java.{util => ju}

class SizeBoundedQueue[E](sizeF: E => Long, size: Long, queue: ju.Queue[E]) extends BlockingQueue[E]
{
  // Used space size
  private val _used = new AtomicLong

  // Returns used space in bytes
  def used() = _used.get()

  protected def enqueue(elem: E): Boolean = {
    if (elem == null) {
      throw new NullPointerException("Can't put null element")
    }

    val elemTotalLength = sizeF(elem)
    if (size < elemTotalLength) {
      // There is no way to get this element ever appended so let's fail fast
      throw new IllegalStateException("Element too big to enqueue")
    }

    if (size - _used.get() < elemTotalLength) {
      false
    } else {

      queue.add(elem)

      _used.addAndGet(elemTotalLength)
      _count.incrementAndGet()

      true
    }
  }

  protected def dequeue(): Option[E] = {
    if (_used.get() == 0) {
      None
    } else {

      val elem = queue.remove()

      val elemTotalLength = sizeF(elem)

      _used.addAndGet(-elemTotalLength)
      _count.decrementAndGet()

      Some(elem)
    }
  }
}
