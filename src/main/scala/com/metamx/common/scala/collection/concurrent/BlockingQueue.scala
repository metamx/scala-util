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

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import scala.collection.mutable.ListBuffer
import com.metamx.common.scala.concurrent.locks.LockOps

abstract class BlockingQueue[E]
{
  // Total elements in queue
  protected val _count = new AtomicInteger

  // Protects put/take and offer/poll
  private val lock = new ReentrantLock()

  // Signals waiting put/offer and take/poll
  private val condition = lock.newCondition()

  // Tries to insert an element non-blocking way, returns whether the insert was successful.
  def offer(elem: E): Boolean = {
    lock {
      val result = enqueue(elem)
      condition.signalAll()
      result
    }
  }

  // Tries to fetch an element non-blocking way, returns None if there is no elements in the queue and
  // Some(element) if an element was retrieved.
  def poll(): Option[E] = {
    lock {
      val result = dequeue()
      condition.signalAll()
      result
    }
  }

  // Puts element blocking way.
  def put(elem: E) {
    lock {
      while (!enqueue(elem)) {
        condition.await()
      }
      condition.signalAll()
    }
  }

  // Takes element blocking way, returns fetched element.
  def take(): E = {
    lock {
      var result: Option[E] = None
      while (result.isEmpty) {
        result = dequeue()
        if (result.isEmpty) {
          condition.await()
        }
      }
      condition.signalAll()
      result.get
    }
  }

  // Returns total elements count in the queue
  def count() = _count.get()

  // Removes all elements from the queue and returns them
  def drain(maxElements: Int = Int.MaxValue): List[E] = {
    val size = math.min(maxElements, _count.get())

    val buf = new ListBuffer[E]()
    buf.sizeHint(size)

    var elem: Option[E] = None
    var i: Int = 0

    while ({
      if (i < size) {
        elem = poll()
        elem.isDefined
      } else {
        false
      }
    }) {
      buf.append(elem.get)
      i += 1
    }

    buf.result()
  }

  protected def enqueue(elem: E): Boolean

  protected def dequeue(): Option[E]
}
