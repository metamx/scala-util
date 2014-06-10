package com.metamx.common.scala

import com.google.common.collect.ImmutableList
import com.metamx.common.scala.untyped.Dict
import com.simple.simplespec.Matchers
import org.junit.Test
import scala.collection.JavaConverters._

class JacksonTest extends Matchers
{
  @Test
  def testSimple()
  {
    val json = """{"hey":"what"}"""
    Jackson.parse[Dict](json) must be(Map("hey" -> "what"))
    Jackson.parse[Dict](json.getBytes) must be(Map("hey" -> "what"))
    Jackson.parse[java.util.Map[String, AnyRef]](json) must be(Map[String, AnyRef]("hey" -> "what").asJava)
    Jackson.generate(Jackson.parse[AnyRef](json)) must be(json)
    Jackson.bytes(Jackson.parse[AnyRef](json)) must be(json.getBytes)
    Jackson.normalize[Dict](
      Map(
        "hey" -> ImmutableList.of("what"),
        "foo" -> None
      )
    ) must be(
      Dict(
        "hey" -> Seq("what"),
        "foo" -> null
      )
    )
  }

  @Test
  def testNulls()
  {
    val json = """{"hey":[null]}"""
    Jackson.parse[Dict](json) must be(Map("hey" -> Seq(null)))
  }
}
