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

import com.metamx.common.scala.concurrent._
import com.simple.simplespec.Spec
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicLong
import scala.util.Random
import org.junit.Test
import java.{util => ju}

class BlockingQueueSpec extends Spec
{

  class A
  {
    @Test def testST() {
      testMT(size => new ByteBufferQueue(ByteBuffer.allocate(size)))
      testMT(size => new SizeBoundedQueue[Array[Byte]](arr => arr.length + 4, size, new ju.LinkedList[Array[Byte]]))
    }

    @Test def testMT() {
      testMT(size => new ByteBufferQueue(ByteBuffer.allocate(size)))
      testMT(size => new SizeBoundedQueue[Array[Byte]](arr => arr.length + 4, size, new ju.LinkedList[Array[Byte]]))
    }

    def testST(qF: (Int) => BlockingQueue[Array[Byte]]) {

      {
        val q = qF(10)

        q.poll().map(_.toList) must be(None)

        q.offer(Array(111.toByte, -37.toByte)) must be(true)
        q.offer(Array(14.toByte, 14.toByte)) must be(false)
        q.offer(Array(14.toByte, 14.toByte)) must be(false)
        q.poll().map(_.toList) must be(Some(List(111.toByte, -37.toByte)))
        q.poll().map(_.toList) must be(None)

        q.offer(Array(11.toByte, -3.toByte)) must be(true)
        q.poll().map(_.toList) must be(Some(List(11.toByte, -3.toByte)))

        q.offer(Array(7.toByte, -100.toByte)) must be(true)
        q.poll().map(_.toList) must be(Some(List(7.toByte, -100.toByte)))
        q.poll().map(_.toList) must be(None)

        q.offer(Array(11.toByte, 15.toByte, -20.toByte)) must be(true)
        q.poll().map(_.toList) must be(Some(List(11.toByte, 15.toByte, -20.toByte)))
        q.poll().map(_.toList) must be(None)

        q.offer(Array(11.toByte, 15.toByte, -20.toByte)) must be(true)
        q.offer(Array(14.toByte, 14.toByte)) must be(false)
        q.offer(Array(14.toByte, 14.toByte)) must be(false)
        q.poll().map(_.toList) must be(Some(List(11.toByte, 15.toByte, -20.toByte)))

        q.offer(Array(11.toByte, 15.toByte, -20.toByte)) must be(true)
        q.poll().map(_.toList) must be(Some(List(11.toByte, 15.toByte, -20.toByte)))
        q.drain(1).map(_.toList) must be(List())
        q.poll().map(_.toList) must be(None)
        q.drain().map(_.toList) must be(List())

        q.offer(Array(15.toByte, 20.toByte)) must be(true)
        q.offer(Array()) must be(true)
        q.poll().map(_.toList) must be(Some(List(15.toByte, 20.toByte)))
        q.poll().map(_.toList) must be(Some(List()))

        q.offer(Array(11.toByte)) must be(true)
        q.offer(Array(102.toByte)) must be(true)
        q.offer(Array(14.toByte)) must be(false)
        q.offer(Array(14.toByte, 14.toByte)) must be(false)
        q.offer(Array(14.toByte, 14.toByte)) must be(false)
        q.drain().map(_.toList) must be(
          List(
            List(11.toByte),
            List(102.toByte)
          )
        )
      }

      {
        val q = qF(23)

        q.offer(Array(111.toByte, -37.toByte, 11.toByte)) must be(true)
        q.offer(Array(127.toByte, -128.toByte)) must be(true)
        q.offer(Array(0.toByte)) must be(true)
        q.offer(Array(0.toByte, 10.toByte)) must be(false)
        q.offer(Array(3.toByte)) must be(true)
        q.drain(1).map(_.toList) must be(List(List(111.toByte, -37.toByte, 11.toByte)))
        q.offer(Array(0.toByte, 10.toByte, 12.toByte, 11.toByte)) must be(false)
        q.offer(Array(0.toByte, 10.toByte)) must be(true)
        q.poll().map(_.toList) must be(Some(List(127.toByte, -128.toByte)))
        q.poll().map(_.toList) must be(Some(List(0.toByte)))
        q.poll().map(_.toList) must be(Some(List(3.toByte)))
        q.poll().map(_.toList) must be(Some(List(0.toByte, 10.toByte)))
        q.poll().map(_.toList) must be(None)
      }
    }

    def testMT(qF: (Int) => BlockingQueue[Array[Byte]]) {

      for (queueSize <- Seq(12, 100, 10000, 1000000)) {
        val q = qF(queueSize)

        val putData = List(
          Array(0.toByte, 10.toByte, 111.toByte, -50.toByte, 10.toByte, 31.toByte, 65.toByte, -128.toByte),
          Array(0.toByte, 10.toByte, 111.toByte, -50.toByte),
          Array(22.toByte, 73.toByte),
          Array(14.toByte),
          Array(2.toByte, 43.toByte)
        )

        // We have a separate take data to be able to check if the tests are catching errors at all
        val takeData = List(
          Array(0.toByte, 10.toByte, 111.toByte, -50.toByte, 10.toByte, 31.toByte, 65.toByte, -128.toByte),
          Array(0.toByte, 10.toByte, 111.toByte, -50.toByte),
          Array(22.toByte, 73.toByte),
          Array(14.toByte),
          Array(2.toByte, 43.toByte)
        )

        // --------------------------------

        val putTakeRepetitions = 3127

        val putThread = thread(
        {
          val r = new Random()
          for (i <- 0 until putTakeRepetitions) {
            for (elem <- putData) {
              q.put(elem)
            }
            if (i % 97 == 0) {
              Thread.sleep(r.nextInt(111))
            }
          }
        }
        )

        val takeThread = thread(
        {
          val r = new Random()
          for (i <- 0 until putTakeRepetitions) {
            for (elem <- takeData) {
              q.take().toList must be(elem.toList)
            }
            if (i % 121 == 0) {
              Thread.sleep(r.nextInt(99))
            }
          }
        }
        )

        // --------------------------------

        val offerPollRepetitions = 2043
        val offerFailed = new AtomicLong
        val pollFailed = new AtomicLong

        val offerThread = thread(
        {
          val r = new Random()
          for (i <- 0 until offerPollRepetitions) {
            for (elem <- putData) {
              while (!q.offer(elem)) {
                offerFailed.incrementAndGet()
              }
            }
            if (i % 45 == 0) {
              Thread.sleep(r.nextInt(33))
            }
          }
        }
        )

        val pollThread = thread(
        {
          val r = new Random()
          for (i <- 0 until offerPollRepetitions) {
            for (elem <- takeData) {
              var result = q.poll()
              while (result.isEmpty) {
                result = q.poll()
                pollFailed.incrementAndGet()
              }
              result.get.toList must be(elem.toList)
            }
            if (i % 217 == 0) {
              Thread.sleep(r.nextInt(193))
            }
          }
        }
        )

        // --------------------------------

        val offerPollPutTakeRepetitions = 1249
        val offerPutFailed = new AtomicLong
        val pollTakeFailed = new AtomicLong

        val offerPutThread = thread(
        {
          val r = new Random()
          for (i <- 0 until offerPollPutTakeRepetitions) {
            for (elem <- putData) {
              if (r.nextInt(4) == 0) {
                while (!q.offer(elem)) {
                  offerPutFailed.incrementAndGet()
                }
              } else {
                q.put(elem)
              }
            }
            if (i % 31 == 0) {
              Thread.sleep(r.nextInt(30))
            }
          }
        }
        )

        val pollTakeThread = thread(
        {
          val r = new Random()
          for (i <- 0 until offerPollPutTakeRepetitions) {
            for (elem <- takeData) {
              val res = if (r.nextInt(7) == 0) {
                var result = q.poll()
                while (result.isEmpty) {
                  result = q.poll()
                  pollTakeFailed.incrementAndGet()
                }
                result.get
              } else {
                q.take()
              }
              res.toList must be(elem.toList)
            }
            if (i % 49 == 0) {
              Thread.sleep(r.nextInt(88))
            }
          }
        }
        )

        // --------------------------------

        putThread.start()
        takeThread.start()
        putThread.join()
        takeThread.join()
        println(
          "<%s> Put-Take completed.".
            format(queueSize)
        )

        offerThread.start()
        pollThread.start()
        offerThread.join()
        pollThread.join()
        println(
          "<%s> Offer-Poll completed. Offer failed: %s; Poll failed: %s.".
            format(queueSize, offerFailed.longValue(), pollFailed.longValue())
        )

        offerPutThread.start()
        pollTakeThread.start()
        offerPutThread.join()
        pollTakeThread.join()
        println(
          "<%s> OfferPut-PollTake completed. OfferPut failed: %s; PollTake failed: %s.".
            format(queueSize, offerPutFailed.longValue(), pollTakeFailed.longValue())
        )
      }
    }

    private def thread(f: => Any) = new Thread(abortingRunnable(f))
  }

}
