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

import com.google.common.primitives.Ints
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong

class ByteBufferQueue(buffer: ByteBuffer) extends BlockingQueue[Array[Byte]]
{
  // Header (which is simply an integer length) size
  private val headerLength = 4

  // Maximum buffer size
  private val size = buffer.capacity()

  // Index of the oldest element
  private var start: Int = 0

  // Index of the next write position
  private var end: Int = 0

  // Used space size
  private val _used = new AtomicLong

  // Returns used space in bytes
  def used() = _used.get()

  override protected def enqueue(elem: Array[Byte]): Boolean = {
    if (elem == null) {
      throw new NullPointerException("Can't put null element")
    }

    val elemTotalLength = headerLength + elem.length
    if (size < elemTotalLength) {
      // There is no way to get this element ever appended so let's fail fast
      throw new IllegalStateException("Element too big to enqueue")
    }

    if (size - _used.get() < elemTotalLength) {
      false
    } else {

      val hdr = Ints.toByteArray(elem.length)

      writeArray(end, hdr)
      writeArray(end + headerLength, elem)

      _used.addAndGet(elemTotalLength)
      _count.incrementAndGet()

      end = (end + elemTotalLength) % size

      true
    }
  }

  override protected def dequeue(): Option[Array[Byte]] = {
    if (_used.get() == 0) {
      None
    } else {

      val hdr = readArray(start, headerLength)
      val length = Ints.fromByteArray(hdr)
      val elem = readArray(start + headerLength, length)

      val elemTotalLength = headerLength + elem.length

      _used.addAndGet(-elemTotalLength)
      _count.decrementAndGet()

      start = (start + elemTotalLength) % size

      Some(elem)
    }
  }

  private def writeArray(offset: Int, arr: Array[Byte]) {
    for (i <- 0 until arr.length) {
      buffer.put((offset + i) % size, arr(i))
    }
  }

  private def readArray(offset: Int, length: Int): Array[Byte] = {
    val arr = new Array[Byte](length)
    for (i <- 0 until arr.length) {
      arr(i) = buffer.get((offset + i) % size)
    }
    arr
  }
}
