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

import com.metamx.common.scala.collection.mutable.ConcurrentMap

/**
 * PermanentMaps are like ConcurrentMaps that only support get and getOrElseUpdate, and for which getOrElseUpdate is
 * guaranteed to execute the "update" function at most once, even if called concurrently from multiple threads.
 */
class PermanentMap[K, V]
{
  private val keyLocks = ConcurrentMap[K, AnyRef]() // Want fine-grained locking, per key.
  private val backingMap = ConcurrentMap[K, V]()

  def apply(key: K, makeFn: () => V): V = getOrElseUpdate(key, makeFn())

  def get(key: K): Option[V] = backingMap.get(key)

  def getOrElseUpdate(key: K, op: => V): V = {
    backingMap.get(key) match {
      case Some(x) => x
      case None =>
        keyLocks.putIfAbsent(key, new AnyRef) // Atomic
        keyLocks(key).synchronized {
          backingMap.getOrElseUpdate(key, op) // Non-atomic, needs to be synchronized
        }
    }
  }
}
