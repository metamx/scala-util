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

import com.metamx.common.lifecycle.Lifecycle
import com.metamx.common.lifecycle.Lifecycle.Handler

object lifecycle {

  class LifecycleOps(lifecycle: Lifecycle) {
    def apply[X](x: X): X = lifecycle.addManagedInstance(x)

    def onStart(f: => Any) = {
      lifecycle.addHandler(
        new Handler {
          def start() {
            f
          }

          def stop() {}
        }
      )
      lifecycle
    }

    def onStop(f: => Any) = {
      lifecycle.addHandler(
        new Handler {
          def start() {}

          def stop() {
            f
          }
        }
      )
      lifecycle
    }
  }
  implicit def LifecycleOps(x: Lifecycle) = new LifecycleOps(x)

}
