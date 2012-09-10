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

import com.codahale.jerkson
import com.metamx.common.scala.exception._
import java.io.File
import java.io.FilterOutputStream
import java.io.FilterWriter
import java.io.OutputStream
import java.io.StringWriter
import java.io.Writer
import org.codehaus.jackson.JsonEncoding
import org.codehaus.jackson.JsonGenerator

object Json extends Json

trait Json extends jerkson.Json {

  def parseOption[A](x: String)(implicit mf: Manifest[A]): Option[A] = parse[A](x).catchOption[Exception]

  // These pretty defs are copy-paste-rename from the jerkson.Generator.generate defs, so that we can add
  // generate.useDefaultPrettyPrinter in pretty[A](A,JsonGenerator).
  // TODO Abstract commonalities between pretty and generate and push upstream.

  def pretty[A](obj: A): String = {
    val writer = new StringWriter
    pretty(obj, writer)
    writer.toString
  }

  def pretty[A](obj: A, output: Writer) {
    pretty(obj, factory.createJsonGenerator(output))
  }

  def pretty[A](obj: A, output: OutputStream) {
    pretty(obj, factory.createJsonGenerator(output, JsonEncoding.UTF8))
  }

  def pretty[A](obj: A, output: File) {
    pretty(obj, factory.createJsonGenerator(output, JsonEncoding.UTF8))
  }

  def pretty[A](obj: A, generator: JsonGenerator) {
    generator.useDefaultPrettyPrinter()                // <-- Here is the only addition
    generator.writeObject(obj)
    generator.close() // (I disagree with this behavior, but it's what Json.generate does)
  }

}

// Json.generate and Json.pretty automatically close their JsonGenerator. Use these to prevent that.
class NoCloseWriter       (x: Writer)       extends FilterWriter(x)       { override def close {} }
class NoCloseOutputStream (x: OutputStream) extends FilterOutputStream(x) { override def close {} }
