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

import com.metamx.common.scala.Predef._
import com.metamx.common.scala.collection.mutable.ConcurrentMap
import com.metamx.common.scala.option._

class PermanentMap[K, V]
{
  private val lock = new AnyRef
  private val locks = ConcurrentMap[K, AnyRef]()
  private val backingMap = ConcurrentMap[K, V]()

  def apply(key: K, makeFn: () => V): V = {
    backingMap.get(key) match {
      case Some(x) => x
      case None =>
        val value = lock.synchronized {
          backingMap.get(key) withEffect {
            _.ifEmpty {
              locks.put(key, new AnyRef)
            }
          }
        }

        value match {
          case Some(x) => x
          case None => locks.get(key).get.synchronized {
            makeFn() withEffect {
              x => backingMap.put(key, x)
            }
          }
        }
    }
  }
}
