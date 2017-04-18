package com.metamx.common.scala.event

import com.github.nscala_time.time.Imports._
import com.google.common.collect.ImmutableMap
import com.metamx.common.scala.untyped._
import com.metamx.emitter.service.{ServiceEventBuilder, ServiceMetricEvent}

// A partially constructed ServiceEventBuilder[ServiceMetricEvent]. Immutable, unlike ServiceMetricEvent.Builder.
case class Metric(
  metric:   String,
  value:    Number,
  userDims: Map[String, Iterable[String]],
  created:  DateTime,
  feed: String
) extends ServiceEventBuilder[ServiceMetricEvent]
{

  // Join two partially constructed metrics; throw IllegalArgumentException if any field is defined on both
  def +(that: Metric): Metric = {
    def onlyOne[X >: Null](x: X, y: X, desc: String): X = (Option(x), Option(y)) match {
      case (None, None) => null
      case (Some(x), None) => x
      case (None, Some(y)) => y
      case (Some(x), Some(y)) => throw new IllegalArgumentException(
        "%s already defined as %s, refusing to shadow with %s" format(desc, x, y)
      )
    }

    def onlyOneOrEqual[X >: Null](x: X, y: X, desc: String): X = (Option(x), Option(y)) match {
      case (None, None) => null
      case (Some(x), None) => x
      case (None, Some(y)) => y
      case (Some(x), Some(y)) if x == y => x
      case (Some(x), Some(y)) => throw new IllegalArgumentException(
        "%s already defined as %s, refusing to shadow with %s" format(desc, x, y)
      )
    }

    def intersectMap(x: Map[String, Iterable[String]], y: Map[String, Iterable[String]]): Map[String, Iterable[String]] =
    {
      x.keySet intersect y.keySet match {
        case xy if xy.isEmpty => x ++ y
        case xy => throw new IllegalArgumentException(
          "userDims has common keys: %s" format xy.mkString(",")
        )
      }
    }

    new Metric(
      metric = onlyOne(this.metric, that.metric, "metric"),
      value = onlyOne(this.value, that.value, "value"),
      userDims = intersectMap(this.userDims, that.userDims),
      created = onlyOne(this.created, that.created, "created"),
      feed = onlyOneOrEqual(this.feed, that.feed, "feed")
    )
  }

  def +(name: String, value: String): Metric = {
    this + Metric(userDims = Map(name -> Seq(value)))
  }

  def +(name: String, value: Iterable[String]): Metric = {
    this + Metric(userDims = Map(name -> value))
  }

  override def build(service: String, host: String): ServiceMetricEvent = {
    build(ImmutableMap.of("service", noNull(service), "host", noNull(host)))
  }

  // Build into a ServiceMetricEvent, throwing NullPointerException if any required field is null
  override def build(serviceDimensions: ImmutableMap[String, String]): ServiceMetricEvent = {
    val builder = ServiceMetricEvent.builder()
    Option(feed).foreach(builder.setFeed)
    userDims.foreach { case (k, v) => builder.setDimension(k, v.toArray) }

    builder.build(created, noNull(metric), noNull(value)).build(serviceDimensions)
  }
}

//This object for backward compatibility with old stuff
object Metric
{
  @deprecated("userX dimensions are deprecated", "1.13.2")
  def apply(
    metric:   String = null,
    value:    Number = null,
    user1:    Iterable[String] = null,
    user2:    Iterable[String] = null,
    user3:    Iterable[String] = null,
    user4:    Iterable[String] = null,
    user5:    Iterable[String] = null,
    user6:    Iterable[String] = null,
    user7:    Iterable[String] = null,
    user8:    Iterable[String] = null,
    user9:    Iterable[String] = null,
    user10:   Iterable[String] = null,
    created:  DateTime = null,
    userDims: Map[String, Iterable[String]] = Map.empty,
    feed: String = null
  ) =
  {
    var result = new Metric(metric, value, userDims, created, feed)

    if (user1  != null) { result = result + ("user1",  user1)  }
    if (user2  != null) { result = result + ("user2",  user2)  }
    if (user3  != null) { result = result + ("user3",  user3)  }
    if (user4  != null) { result = result + ("user4",  user4)  }
    if (user5  != null) { result = result + ("user5",  user5)  }
    if (user6  != null) { result = result + ("user6",  user6)  }
    if (user7  != null) { result = result + ("user7",  user7)  }
    if (user8  != null) { result = result + ("user8",  user8)  }
    if (user9  != null) { result = result + ("user9",  user9)  }
    if (user10 != null) { result = result + ("user10", user10) }

    result
  }
}
