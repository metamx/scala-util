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

object pretty {

  // Truncate a string with "...". Useful for stuffing potentially huge .toString's into log lines.
  def truncate(s: String, n: Int = 500): String = if (s.length > n) s.take(n) + "..." else s

  // e.g. parseBytes("5 GB") == 5L * 1024*1024*1024
  def parseBytes(s: String): Long = ("""\s*(\d+)\s*([A-Z]+)?\s*""".r.unapplySeq(s) : @unchecked) match {
    case None                => throw new IllegalArgumentException("Can't parse bytes: %s" format s)
    case Some(List(n, null)) => parseBytes(s + "B")
    case Some(List(n, suf))  => Seq("B","KB","MB","GB","TB","PB","EB","ZB","YB","BB").indexOf(suf) match {
      case -1  => throw new IllegalArgumentException("Unknown bytes suffix %s in: %s" format (suf, s))
      case exp => n.toLong * math.pow(1024L, exp).toLong
    }
  }

}
