/*
 * Licensed to Metamarkets Group Inc. (Metamarkets) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Metamarkets licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.metamx.common.scala.collection

import com.metamx.common.scala.collection.mutable._
import com.simple.simplespec.Matchers
import org.junit.Test

class MutableTest extends Matchers
{

  @Test def concurrentMap {
    val m = ConcurrentMap("foo" -> 3)
    m.putIfAbsent("foo", 4)
    m.putIfAbsent("bar", 4)
    m.toMap must be(Map("foo" -> 3, "bar" -> 4))
  }

  @Test def multiMap {
    val m = MultiMap("foo" -> 3, "foo" -> 4, "bar" -> 5)
    m.entryExists("foo", _ == 3) must be(true)
    m.entryExists("foo", _ == 4) must be(true)
    m.entryExists("bar", _ == 5) must be(true)
    m.entryExists("bar", _ == 6) must be(false)
  }

}
