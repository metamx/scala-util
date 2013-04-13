package com.metamx.common.scala.event

import org.scala_tools.time.Imports._
import com.metamx.emitter.service.{ServiceMetricEvent, ServiceEventBuilder}
import com.metamx.common.scala.untyped._

// A partially constructed ServiceEventBuilder[ServiceMetricEvent]. Immutable, unlike ServiceMetricEvent.Builder.
case class Metric(
  metric:  String           = null,
  value:   Number           = null,
  user1:   Iterable[String] = null,
  user2:   Iterable[String] = null,
  user3:   Iterable[String] = null,
  user4:   Iterable[String] = null,
  user5:   Iterable[String] = null,
  user6:   Iterable[String] = null,
  user7:   Iterable[String] = null,
  user8:   Iterable[String] = null,
  user9:   Iterable[String] = null,
  user10:  Iterable[String] = null,
  created: DateTime         = null
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
    Metric(
      metric  = f(this.metric,  that.metric,  "metric"),
      value   = f(this.value,   that.value,   "value"),
      user1   = f(this.user1,   that.user1,   "user1"),
      user2   = f(this.user2,   that.user2,   "user2"),
      user3   = f(this.user3,   that.user3,   "user3"),
      user4   = f(this.user4,   that.user4,   "user4"),
      user5   = f(this.user5,   that.user5,   "user5"),
      user6   = f(this.user6,   that.user6,   "user6"),
      user7   = f(this.user7,   that.user7,   "user7"),
      user8   = f(this.user8,   that.user8,   "user8"),
      user9   = f(this.user9,   that.user9,   "user9"),
      user10  = f(this.user10,  that.user10,  "user10"),
      created = f(this.created, that.created, "created")
    )
  }

  // Build into a ServiceMetricEvent, throwing NullPointerException if any required field is null
  override def build(service: String, host: String) = {
    val builder = ServiceMetricEvent.builder()

    if (user1  != null) { builder.setUser1  (user1.toArray)  }
    if (user2  != null) { builder.setUser2  (user2.toArray)  }
    if (user3  != null) { builder.setUser3  (user3.toArray)  }
    if (user4  != null) { builder.setUser4  (user4.toArray)  }
    if (user5  != null) { builder.setUser5  (user5.toArray)  }
    if (user6  != null) { builder.setUser6  (user6.toArray)  }
    if (user7  != null) { builder.setUser7  (user7.toArray)  }
    if (user8  != null) { builder.setUser8  (user8.toArray)  }
    if (user9  != null) { builder.setUser9  (user9.toArray)  }
    if (user10 != null) { builder.setUser10 (user10.toArray) }

    builder.build(created, noNull(metric), noNull(value))
      .build(noNull(service), noNull(host))
  }

}
