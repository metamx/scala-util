/*
 * Copyright 2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.scala

import com.metamx.common.scala.Logging.Logger
import com.metamx.common.scala.Predef._
import com.metamx.common.scala.untyped.Dict
import com.metamx.common.scala.untyped.noNull
import com.metamx.common.scala.untyped.normalizeJava
import com.metamx.emitter.service.AlertEvent
import com.metamx.emitter.service.AlertEvent.Severity
import com.metamx.emitter.service.AlertEvent.Severity.{ANOMALY, COMPONENT_FAILURE, SERVICE_FAILURE}
import com.metamx.emitter.service.ServiceEmitter
import com.metamx.emitter.service.ServiceEventBuilder
import com.metamx.emitter.service.ServiceMetricEvent
import java.{util => ju}
import org.codehaus.jackson.map.ObjectMapper
import org.joda.time.DateTime
import scala.collection.JavaConverters._

// TODO Most, if not all, of this should be pushed up into the emitter library (or emitter-scala)

object event {

  val WARN  = ANOMALY
  val ERROR = COMPONENT_FAILURE

  def emitAlert(
    e:           Throwable,
    log:         Logger,
    emitter:     ServiceEmitter,
    severity:    Severity,
    description: String,
    data:        Dict
  ) {
    normalizeJavaViaJson(data ++ Option(e).map("exception" -> _.toString)) into { data =>
      ((if (severity == ANOMALY) log.warn(_,_) else log.error(_,_)): (Throwable, String) => Unit)(
        e, "Emitting alert: [%s] %s\n%s" format (severity, description, Json.pretty(data))
      )
      emitter.emit(new AlertEvent.Builder().build(severity, description, data.asInstanceOf[ju.Map[String, AnyRef]]))
    }
  }

  def emitAlert(log: Logger, emitter: ServiceEmitter, severity: Severity, description: String, data: Dict) {
    emitAlert(null, log, emitter, severity, description, data)
  }

  // HACK: Map scala-native types to java types by writing out through jerkson and reading back in through jackson.
  // This will not only normalize scala collections, which untyped.normalizeJava knows how to do, but also things like
  // Option and Either, which untyped.normalizeJava doesn't and shouldn't know how to do. (It would be nice if we could
  // wholly extract the scala->java behavior out of jerkson...)
  def normalizeJavaViaJson(x: Any): Any = jacksonMapper.readValue(Json.generate(x), classOf[Any])
  lazy val jacksonMapper = new ObjectMapper

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
      def f[X >: Null](x: X, y: X, desc: String): X = (Option(x) ++ Option(y)) match {
        case Seq()    => null
        case Seq(z)   => z
        case Seq(_,_) => throw new IllegalArgumentException(
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
    override def build(service: String, host: String) = new ServiceMetricEvent(
      created, // ServiceMetricEvent maps null -> DateTime.now
      noNull(service),
      noNull(host),
      user1  mapNonNull (_.toArray),
      user2  mapNonNull (_.toArray),
      user3  mapNonNull (_.toArray),
      user4  mapNonNull (_.toArray),
      user5  mapNonNull (_.toArray),
      user6  mapNonNull (_.toArray),
      user7  mapNonNull (_.toArray),
      user8  mapNonNull (_.toArray),
      user9  mapNonNull (_.toArray),
      user10 mapNonNull (_.toArray),
      noNull(metric),
      noNull(value)
    )

  }

}
