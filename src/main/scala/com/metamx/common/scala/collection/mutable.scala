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

import java.util.concurrent.ConcurrentHashMap
import scala.collection.JavaConversions.asScalaConcurrentMap
import scala.collection.{mutable => _mutable}
import scala.collection.mutable.HashMap

import com.metamx.common.scala.Predef.EffectOps

object mutable {

  // A scala-friendly way to create a ConcurrentMap backed by a juc.ConcurrentHashMap
  object ConcurrentMap {
    def apply[K,V](xs: (K,V)*): ConcurrentMap[K,V] = asScalaConcurrentMap(new ConcurrentHashMap()) withEffect {
      _ ++= xs
    }
  }
  type ConcurrentMap[K,V] = _mutable.ConcurrentMap[K,V]

  // A more friendly way to create a MultiMap
  object MultiMap {
    def apply[K,V](xs: (K,V)*): MultiMap[K,V] = new HashMap[K, _mutable.Set[V]] with MultiMap[K,V] withEffect { m =>
      xs foreach { case (k,v) => m.addBinding(k,v) }
    }
  }
  type MultiMap[K,V] = _mutable.MultiMap[K,V]

}
