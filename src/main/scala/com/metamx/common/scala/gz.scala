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

import com.google.common.io.ByteStreams
import com.metamx.common.scala.Predef.EffectOps
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

object gz {

  def gzip(bytes: Array[Byte]): Array[Byte] = {
    new ByteArrayOutputStream withEffect { out =>
      new GZIPOutputStream(out) withEffect { gz =>
        gz.write(bytes)
        gz.close
      }
    } toByteArray
  }

  def gunzip(bytes: Array[Byte]): Array[Byte] = {
    ByteStreams.toByteArray(new GZIPInputStream(new ByteArrayInputStream(bytes)))
  }

  def gunzipIfNecessary(bytes: Array[Byte]): Array[Byte] = {
    if (isGzip(bytes)) gunzip(bytes) else bytes
  }

  def isGzip(bytes: Array[Byte]): Boolean = {
    bytes.length >= 2 &&
    bytes(0) == (GZIPInputStream.GZIP_MAGIC >> 0).toByte &&
    bytes(1) == (GZIPInputStream.GZIP_MAGIC >> 8).toByte
  }

}
