package com.metamx.common.scala

import com.fasterxml.jackson.core.{JsonFactory, JsonGenerator, JsonParser}
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.metamx.common.scala.Predef._
import java.io.{FilterOutputStream, FilterWriter, InputStream, OutputStream, Reader, Writer}

object Jackson extends Jackson

trait Jackson
{
  private val objectMapper = newObjectMapper()

  def parse[A: ClassManifest](s: String): A = {
    objectMapper.readValue(s, implicitly[ClassManifest[A]].erasure.asInstanceOf[Class[A]])
  }

  def parse[A: ClassManifest](bs: Array[Byte]): A = {
    objectMapper.readValue(bs, implicitly[ClassManifest[A]].erasure.asInstanceOf[Class[A]])
  }

  def parse[A: ClassManifest](reader: Reader): A = {
    objectMapper.readValue(reader, implicitly[ClassManifest[A]].erasure.asInstanceOf[Class[A]])
  }

  def parse[A: ClassManifest](stream: InputStream): A = {
    objectMapper.readValue(stream, implicitly[ClassManifest[A]].erasure.asInstanceOf[Class[A]])
  }

  def parse[A: ClassManifest](jp: JsonParser): A = {
    objectMapper.readValue(jp, implicitly[ClassManifest[A]].erasure.asInstanceOf[Class[A]])
  }

  def generate[A](a: A): String = objectMapper.writeValueAsString(a)

  def generate[A](a: A, writer: Writer) {
    objectMapper.writeValue(writer, a)
  }

  def generate[A](a: A, stream: OutputStream) {
    objectMapper.writeValue(stream, a)
  }

  def generate[A](a: A, jg: JsonGenerator) {
    objectMapper.writeValue(jg, a)
  }

  def bytes[A](a: A): Array[Byte] = objectMapper.writeValueAsBytes(a)

  def pretty[A](a: A): String = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(a)

  def pretty[A](a: A, writer: Writer) {
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, a)
  }

  def pretty[A](a: A, stream: OutputStream) {
    objectMapper.writerWithDefaultPrettyPrinter().writeValue(stream, a)
  }

  def normalize[A : ClassManifest](a: A) = parse[A](generate(a))

  def newObjectMapper(): ObjectMapper = newObjectMapper(null)

  def newObjectMapper(jsonFactory: JsonFactory): ObjectMapper = {
    new ObjectMapper(jsonFactory) withEffect {
      jm =>
        jm.registerModule(new JodaModule)
        jm.registerModule(DefaultScalaModule)
        jm.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
    }
  }
}

// Jackson.generate and Jackson.pretty automatically close their JsonGenerator. Use these to prevent that.
class NoCloseWriter       (x: Writer)       extends FilterWriter(x)       { override def close {} }
class NoCloseOutputStream (x: OutputStream) extends FilterOutputStream(x) { override def close {} }
