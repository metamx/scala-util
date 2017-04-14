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

import org.skife.config.ConfigurationObjectFactory
import scala.reflect.runtime.universe.TypeTag

object config {

  class ConfigOps(configs: ConfigurationObjectFactory) {
    implicit def apply[X](implicit tag: TypeTag[X]): X = configs.build(tag.mirror.runtimeClass(tag.tpe).asInstanceOf[Class[X]])
  }
  implicit def ConfigOps(configs: ConfigurationObjectFactory) = new ConfigOps(configs)

}
