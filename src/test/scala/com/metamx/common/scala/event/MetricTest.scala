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

  @Test
  def testUserDimensions(): Unit = {
    val metric = Metric(
      user1  = Seq("user1"),
      user2  = Seq("user2"),
      user3  = Seq("user3"),
      user4  = Seq("user4"),
      user5  = Seq("user5"),
      user6  = Seq("user6"),
      user7  = Seq("user7"),
      user8  = Seq("user8"),
      user9  = Seq("user9"),
      user10 = Seq("user10")
    )

    metric.userDims.get("user1")  must be(Some(Seq("user1")))
    metric.userDims.get("user2")  must be(Some(Seq("user2")))
    metric.userDims.get("user3")  must be(Some(Seq("user3")))
    metric.userDims.get("user4")  must be(Some(Seq("user4")))
    metric.userDims.get("user5")  must be(Some(Seq("user5")))
    metric.userDims.get("user6")  must be(Some(Seq("user6")))
    metric.userDims.get("user7")  must be(Some(Seq("user7")))
    metric.userDims.get("user8")  must be(Some(Seq("user8")))
    metric.userDims.get("user9")  must be(Some(Seq("user9")))
    metric.userDims.get("user10") must be(Some(Seq("user10")))
  }
}
