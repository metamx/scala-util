package com.metamx.common.scala

import com.metamx.common.scala.chaincast._
import com.simple.simplespec.Spec
import com.metamx.common.scala.untyped.Dict
import org.junit.Test

class chaincastSpec extends Spec
{

  class A
  {
    @Test
    def testSimple() {
      val thing = Json.parse[Dict]("""{"results":[{"k": 1, "k2": 2}, {"k": 2}]}""").chainCast
      thing("results").asList.map(kv => kv.asDict.apply("k").asInt) must be(Seq(1, 2))
      thing("results").asList.flatMap(kv => kv.asDict.get("k2").map(_.asInt)) must be(Seq(2))
      thing("results").asList.head.apply("k").asInt must be(1)
      thing("results").asList.headOption.map(_.apply("k").asInt) must be(Some(1))
    }
  }

}
