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

import com.codahale.simplespec.Spec
import java.io.IOException
import org.junit.Test
import scala.util.Random

import gz._

class gzSpec extends Spec {

  val bytes = Stream.fill(1024) { Random.nextPrintableChar }.mkString("").getBytes

  def str(bytes: Array[Byte]) = new String(bytes)

  class A {

    @Test def empty {

      isGzip(Array())       must be(false)
      isGzip(gzip(Array())) must be(true)

      str(gunzip(gzip(Array())))     must be("")
      evaluating { gunzip(Array()) } must throwAn[IOException]

      str(gunzipIfNecessary(Array())) must be("")

    }

    @Test def nonEmpty {

      isGzip(bytes)                must be(false)
      isGzip(gzip(bytes))          must be(true)
      isGzip(gunzip(gzip(bytes)))  must be(false)
      evaluating { gunzip(bytes) } must throwAn[IOException]

      str(gunzip(gzip(bytes)))     must be(str(bytes))

      str(gunzipIfNecessary(bytes))                    must be(str(bytes))
      str(gunzipIfNecessary(gzip(bytes)))              must be(str(bytes))
      str(gunzipIfNecessary(gunzipIfNecessary(bytes))) must be(str(bytes))

    }

    @Test def doubleZip {
      str(gunzip(gunzip(gzip(gzip(bytes))))) must be(str(bytes))
      str(gunzip(gzip(gzip(bytes))))         must be(str(gzip(bytes)))
    }

  }

}
