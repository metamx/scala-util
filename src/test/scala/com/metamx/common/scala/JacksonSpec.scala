package com.metamx.common.scala

import com.simple.simplespec.Spec
import org.junit.Test
import com.metamx.common.scala.untyped.Dict
import scala.collection.JavaConverters._

class JacksonSpec extends Spec
{
  class A
  {
    @Test
    def testSimple()
    {
      val json = """{"hey":"what"}"""
      Jackson.parse[Dict](json) must be(Map("hey" -> "what"))
      Jackson.parse[Dict](json.getBytes) must be(Map("hey" -> "what"))
      Jackson.parse[java.util.Map[String, AnyRef]](json) must be(Map[String, AnyRef]("hey" -> "what").asJava)
      Jackson.generate(Jackson.parse(json)) must be(json)
      Jackson.bytes(Jackson.parse(json)) must be(json.getBytes)
      Jackson.normalize[Dict](Map("hey" -> Seq("what").asJava)) must be(Map[String, AnyRef]("hey" -> Seq("what")))
    }
  }
}
