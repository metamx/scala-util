package com.metamx.common.scala

import com.metamx.common.scala.chaincast._
import com.metamx.common.scala.untyped.Dict
import com.simple.simplespec.Matchers
import org.junit.Test

class ChaincastTest extends Matchers
{

  @Test
  def testSimple() {
    val thing = Jackson.parse[Dict]("""{"results":[{"k": 1, "k2": 2}, {"k": 2}]}""").chainCast
    thing("results").asList.map(kv => kv.asDict.apply("k").asInt) must be(Seq(1, 2))
    thing("results").asList.flatMap(kv => kv.asDict.get("k2").map(_.asInt)) must be(Seq(2))
    thing("results").asList.head.apply("k").asInt must be(1)
    thing("results").asList.headOption.map(_.apply("k").asInt) must be(Some(1))
  }

}
