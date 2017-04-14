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

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import com.metamx.common.scala.Predef._

@deprecated("Consider java.util.concurrent.* as an alternative.", "1.13.0")
object atomic
{
  @deprecated("SynchronizedMap is deprecated since scala 2.11.0. Consider java.util.concurrent.ConcurrentHashMap as an alternative.", "1.13.0")
  class AtomicMap[A, B] extends mutable.HashMap[A, B] with mutable.SynchronizedMap[A, B]

  @deprecated("SynchronizedBuffer are deprecated since scala 2.11.0. Consider java.util.concurrent.ConcurrentLinkedQueue as an alternative.", "1.13.0")
  class AtomicBuffer[A] extends ArrayBuffer[A] with mutable.SynchronizedBuffer[A]
  {
    def drain() = synchronized {
      toList withEffect {
        _ => clear()
      }
    }
  }
}
