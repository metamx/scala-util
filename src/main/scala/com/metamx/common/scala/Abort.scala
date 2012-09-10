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

object Abort extends Logging {

  def apply(e: Throwable): Nothing = {
    log.error("Aborting: " + e)
    e.printStackTrace
    Runtime.getRuntime.halt(1) // (Avoid System.exit hangs)
    throw new Exception("Unreachable")
  }

  def apply(msg: String): Nothing = {
    log.error("Aborting: " + msg)
    Runtime.getRuntime.halt(1) // (Avoid System.exit hangs)
    throw new Exception("Unreachable")
  }

}
