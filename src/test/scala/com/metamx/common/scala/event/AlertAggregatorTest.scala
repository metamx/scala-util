package com.metamx.common.scala.event

import com.metamx.common.scala.Logging
import com.metamx.common.scala.Predef._
import com.metamx.common.scala.untyped._
import com.metamx.emitter.core.{Emitter, Event}
import com.metamx.emitter.service.ServiceEmitter
import com.simple.simplespec.Matchers
import com.twitter.util.NoStacktrace
import org.junit.Test
import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class AlertAggregatorTest extends Matchers with Logging
{

  class TestException extends Exception("boo!") with NoStacktrace

  def createEmitter(): (Seq[Event], ServiceEmitter) = {
    val buffer = new ArrayBuffer[Event] with mutable.SynchronizedBuffer[Event]
    val emitter = new ServiceEmitter(
      "service", "host", new Emitter
      {
        override def start() {}

        override def flush() {}

        override def emit(event: Event) {
          buffer += event
        }

        override def close() {}
      }
    )
    (buffer, emitter)
  }

  @Test
  def testNothing()
  {
    val (events, emitter) = createEmitter()
    val alerts = new AlertAggregator(log)
    alerts.monitor.withEffect(_.start()).monitor(emitter)
    events must be(Nil)
  }

  @Test
  def testAggregationSimple()
  {
    val (events, emitter) = createEmitter()
    val alerts = new AlertAggregator(log)
    alerts.put("foo", Map("baz" -> 3))
    alerts.put("foo", Map("baz" -> 3))
    alerts.put("foo", Map("baz" -> 3))
    alerts.put("bar", Map("baz" -> 4))
    alerts.monitor.withEffect(_.start()).monitor(emitter)
    events.map(d => dict(normalize(d.toMap.asScala - "timestamp"))).sortBy(x => str(x("description"))) must be(
      Seq(
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "bar",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 4),
            "alertCount" -> 1
          )
        ),
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "foo",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 3),
            "alertCount" -> 3
          )
        )
      )
    )
  }

  @Test
  def testAggregationWithExceptions()
  {
    val (events, emitter) = createEmitter()
    val alerts = new AlertAggregator(log)
    alerts.put(new TestException, "foo", Map("baz" -> 3))
    alerts.put(new TestException, "foo", Map("baz" -> 3))
    alerts.put("foo", Map("baz" -> 3))
    alerts.put("bar", Map("baz" -> 4))
    alerts.monitor.withEffect(_.start()).monitor(emitter)
    val eventDicts = events
      .map(d => dict(normalize(d.toMap.asScala - "timestamp")))
      .sortBy(x => str(x("description")) + str(dict(x("data")).getOrElse("exceptionType", "")))
    eventDicts must be(
      Seq(
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "bar",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 4),
            "alertCount" -> 1
          )
        ),
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "foo",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 3),
            "alertCount" -> 1
          )
        ),
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "foo",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 3),
            "exceptionType" -> (new TestException).getClass.getName,
            "exceptionMessage" -> "boo!",
            "exceptionStackTrace" ->
              Seq(
                "com.metamx.common.scala.event.AlertAggregatorTest$TestException: boo!\n",
                "\tat com.twitter.util.NoStacktrace(Unknown Source)\n"
              ).mkString,
            "alertCount" -> 2
          )
        )
      )
    )
  }

  @Test
  def testAggregationUponAggregation()
  {
    val (events, emitter) = createEmitter()
    val alerts = new AlertAggregator(log)
    alerts.put("foo", Map("baz" -> 3))
    alerts.put("foo", Map("baz" -> 3))
    alerts.monitor.withEffect(_.start()).monitor(emitter)
    alerts.put("foo", Map("baz" -> 3))
    alerts.put("bar", Map("baz" -> 4))
    alerts.monitor.withEffect(_.start()).monitor(emitter)
    events.map(d => dict(normalize(d.toMap.asScala - "timestamp"))).sortBy(x => str(x("description"))) must be(
      Seq(
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "bar",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 4),
            "alertCount" -> 1
          )
        ),
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "foo",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 3),
            "alertCount" -> 2
          )
        ),
        Map(
          "feed" -> "alerts",
          "service" -> "service",
          "host" -> "host",
          "severity" -> "anomaly",
          "description" -> "foo",
          "data" -> Map(
            "sampleDetails" -> Map("baz" -> 3),
            "alertCount" -> 1
          )
        )
      )
    )
  }

}
