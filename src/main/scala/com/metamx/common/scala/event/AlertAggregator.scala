package com.metamx.common.scala.event

import com.metamx.common.scala.Logger
import com.metamx.common.scala.untyped.Dict
import com.metamx.emitter.service.ServiceEmitter
import com.metamx.metrics.{AbstractMonitor, Monitor}
import java.util.concurrent.atomic.AtomicReference
import scala.collection.mutable
import scala.util.Random

/**
 * Aggregates alerts together and can periodically emit and log aggregated alerts. Uses memory linear in the
 * number of (description, exception class) pairs seen between alert emissions. This is meant to make it feasible to
 * report exceptions that may occur at very high rates.
 *
 * To control memory use and noisiness of alerting, it is important to avoid using a wide variety of descriptions.
 *
 * @param log Used to log exceptions
 */
class AlertAggregator(log: Logger, private val rand: Random = new Random)
{
  // (description, exception class)
  type AlertKey = (String, Option[String])

  private val lock   = new AnyRef
  private val alerts = new AtomicReference(mutable.HashMap[AlertKey, AggregatedAlerts]())

  def put(e: Throwable, description: String, data: Dict) {
    put(Option(e), description, data)
  }

  def put(description: String, data: Dict) {
    put(None, description, data)
  }

  private def put(e: Option[Throwable], description: String, data: Dict) {
    val key = (description, e.map(_.getClass.getName))
    lock.synchronized {
      alerts.get().getOrElseUpdate(key, new AggregatedAlerts(description)).put(e, data)
    }
  }

  lazy val monitor: Monitor = new AbstractMonitor {
    override def doMonitor(emitter: ServiceEmitter) = {
      // Swap alert buffers and emit the old ones.
      val snapshot = lock.synchronized {
        alerts.getAndSet(mutable.HashMap())
      }
      for (((description, _), aa) <- snapshot) {
        // emitAlert can handle null exceptions
        emit.emitAlert(
          e = aa.e.orNull,
          log = log,
          emitter = emitter,
          severity = WARN,
          description = description,
          data = Dict(
            "sampleDetails" -> aa.data,
            "alertCount" -> aa.count
          )
        )
      }
      true
    }
  }

  private class AggregatedAlerts(val description: String)
  {
    var count                = 0
    var data: Dict           = Map.empty
    var e: Option[Throwable] = None

    def put(e: Option[Throwable], data: Dict) {
      // Try to get representative data instead of just the first ones.
      count += 1
      if (rand.nextInt(count) == 0) {
        this.data = data
        this.e = e
      }
    }
  }

}
