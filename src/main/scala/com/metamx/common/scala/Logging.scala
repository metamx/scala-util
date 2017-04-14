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

package com.metamx.common.scala

trait Logging {
  // Initialize boolean variable by default value to retain the same behaviour for deserialized class
  @transient @volatile private var initialized: Boolean = _
  @transient private var logger: Logger = _

  // We emulate behaviour of lazy val because scala 2.12.1 has bug with transient lazy val
  // https://issues.scala-lang.org/browse/SI-10244
  def log: Logger = {
    if (initialized) {
      logger
    } else {
      compute
    }
  }

  private def compute: Logger = {
    synchronized {
      if (!initialized) {
        logger = Logger(getClass)
        initialized = true
      }
      logger
    }
  }
}
