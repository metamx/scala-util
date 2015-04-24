package com.metamx.common.scala.event

import org.scala_tools.time.Imports._
import com.metamx.emitter.service.{ServiceMetricEvent, ServiceEventBuilder}
import com.metamx.common.scala.untyped._

// A partially constructed ServiceEventBuilder[ServiceMetricEvent]. Immutable, unlike ServiceMetricEvent.Builder.
case class Metric(
  metric:   String                        = null,
  value:    Number                        = null,
  userDims: Map[String, Iterable[String]] = null,
  created:  DateTime                      = null
  ) extends ServiceEventBuilder[ServiceMetricEvent] {

  // Join two partially constructed metrics; throw IllegalArgumentException if any field is defined on both
  def + (that: Metric): Metric = {
    def f[X >: Null](x: X, y: X, desc: String): X = (Option(x), Option(y)) match {
      case (None,    None)    => null
      case (Some(x), None)    => x
      case (None,    Some(y)) => y
      case (Some(x), Some(y)) => throw new IllegalArgumentException(
        "%s already defined as %s, refusing to shadow with %s" format (desc, x, y)
      )
    }

    def intersectMap(x:Map[String, Iterable[String]], y:Map[String, Iterable[String]]): Map[String, Iterable[String]] = {
      (Option(x), Option(y)) match {
        case (None,    None)    => null
        case (Some(x), None)    => x
        case (None,    Some(y)) => y
        case (Some(x), Some(y)) => {
          x.keySet & y.keySet toSeq match {
            case Seq() => x ++ y
            case r => throw new IllegalArgumentException(
              "userDims has common keys: %s" format r mkString ","
            )
          }
        }
      }
    }

    Metric(
      metric    = f(this.metric,  that.metric,  "metric"),
      value     = f(this.value,   that.value,   "value"),
      userDims  = intersectMap(this.userDims, that.userDims),
      created   = f(this.created, that.created, "created")
    )
  }

  def +(name: String, value: String): Metric = {
    this + Metric(userDims = Map(name -> Seq(value)))
  }

  def +(name: String, value: Iterable[String]): Metric = {
    this + Metric(userDims = Map(name -> value))
  }

  // Build into a ServiceMetricEvent, throwing NullPointerException if any required field is null
  override def build(service: String, host: String) = {
    val builder = ServiceMetricEvent.builder()

    val userDimsOpt = Option(userDims)
    if (userDimsOpt.isDefined) {
      userDimsOpt.get map  {
        case(k, v) => builder.setDimension(k, v.toArray)
      }
    }

    builder.build(created, noNull(metric), noNull(value))
      .build(noNull(service), noNull(host))
  }

}
