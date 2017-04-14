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

package com.metamx.common.concurrent

import com.github.nscala_time.time.Imports._
import com.metamx.common.scala.Logging

class RepeatingThread(delay: Duration, runnable: Runnable)
extends Thread with Logging {

  setDaemon(true)

  @volatile
  private var cancelled = false

  override def run() {
    try {
      while (!cancelled) {
        runnable.run()
        try {
          Thread.sleep(delay.millis)
        } catch {
          case _: InterruptedException =>
            // Continue
        }
      }
    } catch {
      case e: Throwable =>
        log.error(e, "Killed by exception")
    }
  }

  def repeatNow() {
    interrupt()
  }

  def cancel() {
    cancelled = true
    interrupt()
  }

}
