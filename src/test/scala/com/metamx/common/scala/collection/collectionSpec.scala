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

package com.metamx.common.scala.collection

import com.simple.simplespec.Spec
import com.metamx.common.scala.Predef._
import java.{util => ju}
import org.junit.Test
import scala.collection.JavaConverters._

class collectionSpec extends Spec {

  class A {

    @Test def untilEmptyTest {
      var n  = 5
      val xs = untilEmpty {
        0 until n withEffect { _ =>
          n -= 1
        }
      }
      n                  must be(4)
      xs.take(5).toList  must be(List(0,1,2,3,4))
      n                  must be(4)
      xs.take(6).toList  must be(List(0,1,2,3,4, 0))
      n                  must be(3)
      xs.take(10).toList must be(List(0,1,2,3,4, 0,1,2,3, 0))
      n                  must be(2)
      xs.toList          must be(List(0,1,2,3,4, 0,1,2,3, 0,1,2, 0,1, 0))
      n                  must be(-1)
    }

    @Test def onCompleteBasicStream {
      var complete        = false
      val xs: Stream[Int] = Stream(1,2,3) onComplete { _ => complete = true }
      complete  must be(false)
      xs.toList must be(List(1,2,3))
      complete  must be(true)
    }

    @Test def onCompleteBasicIterator {
      var complete          = false
      val xs: Iterator[Int] = Iterator(1,2,3) onComplete { _ => complete = true }
      complete  must be(false)
      xs.toList must be(List(1,2,3))
      complete  must be(true)
    }

    @Test def onCompleteBasicList {
      var complete      = false
      val xs: List[Int] = List(1,2,3) onComplete { _ => complete = true }
      complete  must be(true)
      xs.toList must be(List(1,2,3))
      complete  must be(true)
    }

    @Test def takeUntilStream {
                 { Stream.from(0) map { x => assert(x < 3); x } takeUntil (_ == 2) toList } must be(List(0,1,2))
      evaluating { Stream.from(0) map { x => assert(x < 3); x } takeWhile (_ < 3)  toList } must throwAn[Error]
    }

    @Test def takeUntilIterator {
                 { Iterator.from(0) map { x => assert(x < 3); x } takeUntil (_ == 2) toList } must be(List(0,1,2))
      evaluating { Iterator.from(0) map { x => assert(x < 3); x } takeWhile (_ < 3)  toList } must throwAn[Error]
    }

    @Test def takeUntilList {
      evaluating { List(0,1,2,3) map { x => assert(x < 3); x } takeUntil (_ == 2) toList } must throwAn[Error]
      evaluating { List(0,1,2,3) map { x => assert(x < 3); x } takeWhile (_ < 3)  toList } must throwAn[Error]
    }

  }

}
