package com.metamx.common.scala.counters

import com.simple.simplespec.Matchers
import org.junit.Test

class NumericCountersTest extends Matchers
{
  private def makeMetrics(counters: LongCounters) = {
    counters.snapshotAndReset().map {
      m =>
        val key =
          "%s: %s & (%s)".format(
            m.build("test", "localhost").getFeed,
            m.metric,
            m.userDims.toSeq.map{
              case (dim, values) => "%s -> %s".format(dim, values.mkString(", "))
            }.sorted.mkString("; ")
          )
        key -> m.value.toString
    }.toMap
  }

  private def makeMetrics(counters: DoubleCounters) = {
    counters.snapshotAndReset().map {
      m =>
        val key =
          "%s: %s & (%s)".format(
            m.build("test", "localhost").getFeed,
            m.metric,
            m.userDims.toSeq.map{
              case (dim, values) => "%s -> %s".format(dim, values.mkString(", "))
            }.sorted.mkString("; ")
          )
        key -> "%.0f".format(m.value.doubleValue())
    }.toMap
  }

  @Test def testLong() {
    val counters = new LongCounters

    counters.inc("a", Map("x" -> Seq("1")))
    counters.add("a", Map("x" -> Seq("1")), 2)

    counters.add("b", Map("z" -> Seq("4")), 10000)
    counters.inc("b", Map("z" -> Seq("4")))

    counters.add("a", Map("x" -> Seq("1", "2")), 10)

    counters.add("a", Map("x" -> Seq("1"), "y" -> Seq("3")), 100)

    counters.add("c", Map("x" -> Seq("1")), 10)
    counters.del("c", Map("x" -> Seq("1")))

    makeMetrics(counters) must be (Map(
      "metrics: a & (x -> 1)" -> "3",
      "metrics: a & (x -> 1, 2)" -> "10",
      "metrics: a & (x -> 1; y -> 3)" -> "100",
      "metrics: b & (z -> 4)" -> "10001"
    ))

    counters.inc("a", Map("x" -> Seq("1")))

    makeMetrics(counters) must be (Map(
      "metrics: a & (x -> 1)" -> "1"
    ))
  }

  @Test def testDouble() {
    implicit val counters = new DoubleCounters


    counters.inc("a", Map("x" -> Seq("1")))
    counters.add("a", Map("x" -> Seq("1")), 2.0)

    counters.add("b", Map("z" -> Seq("4")), 10000.0)
    counters.inc("b", Map("z" -> Seq("4")))

    counters.add("a", Map("x" -> Seq("1", "2")), 10.0)

    counters.add("a", Map("x" -> Seq("1"), "y" -> Seq("3")), 100.0)

    counters.add("c", Map("x" -> Seq("1")), 10.0)
    counters.del("c", Map("x" -> Seq("1")))

    makeMetrics(counters) must be (Map(
      "metrics: a & (x -> 1)" -> "3",
      "metrics: a & (x -> 1, 2)" -> "10",
      "metrics: a & (x -> 1; y -> 3)" -> "100",
      "metrics: b & (z -> 4)" -> "10001"
    ))

    counters.inc("a", Map("x" -> Seq("1")))

    makeMetrics(counters) must be (Map(
      "metrics: a & (x -> 1)" -> "1"
    ))
  }

  @Test def testLongWithCustomFeed(): Unit = {
    val counters = new LongCounters("test_feed")

    counters.inc("a", Map("x" -> Seq("1")))
    counters.add("a", Map("x" -> Seq("1")), 2)
    counters.inc("b", Map("z" -> Seq("4")))

    makeMetrics(counters) must be (Map(
      "test_feed: a & (x -> 1)" -> "3",
      "test_feed: b & (z -> 4)" -> "1"
    ))

    counters.inc("a", Map("x" -> Seq("1")))

    makeMetrics(counters) must be (Map(
      "test_feed: a & (x -> 1)" -> "1"
    ))
  }
}
