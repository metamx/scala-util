package com.metamx.common.scala.event

import com.simple.simplespec.Matchers
import org.junit.Test
import org.scala_tools.time.Imports._


class MetricTest extends Matchers
{
  @Test
  def testUnionMetrics() {
    val m1 = Metric(metric = "metric")
    val m2 = Metric(value = 1)

    val res = m1 + m2

    res.metric must be("metric")
    res.value must be(1)
  }

  @Test
  def testUnionWithException() {
    val m1 = Metric(metric = "metric1", value = 1, created = new DateTime())
    val m2 = Metric(metric = "metric2", value = 2, created = new DateTime())

    evaluating {
      m1 + m2
    } must throwAn[IllegalArgumentException]("metric already defined as metric1, refusing to shadow with metric2")
  }

  @Test
  def testUnionDimensions() {
    (Metric() + ("dim", "val")).userDims.get("dim").isDefined must be(true)
    (Metric() + ("dim", Seq("val"))).userDims.get("dim").isDefined must be(true)

    evaluating {
      Metric(userDims = Map("dim" -> Seq("value1"))) + Metric(userDims = Map("dim" -> Seq("value2")))
    } must throwAn[IllegalArgumentException]("userDims has common keys: dim")

    val res = Metric(userDims = Map("dim1" -> Seq("value1"))) +
      Metric(userDims = Map("dim2" -> Seq("value2.1", "value2.2")))

    res.userDims.get("dim1").isDefined must be(true)
    res.userDims.get("dim1") must be(Some(Seq("value1")))

    res.userDims.get("dim2").isDefined must be(true)
    res.userDims.get("dim2") must be(Some(Seq("value2.1", "value2.2")))
  }

  @Test
  def testBuildServiceMetric() {
    val event = Metric(
      metric = "metric",
      value = 1,
      userDims = Map("dim1" -> Seq("value1"), "dim2" -> Seq("value2.1", "value2.2"))
    ).build("test", "localhost")

    event.getService  must be ("test")
    event.getHost     must be ("localhost")
    event.getMetric   must be ("metric")
    event.getValue    must be (1)

    event.getUserDims.containsKey("dim1") must be(true)
    event.getUserDims.containsKey("dim2") must be(true)
  }
}
