package com.metamx.common.scala.counters

import com.metamx.emitter.service.ServiceEmitter
import com.metamx.metrics.AbstractMonitor

/**
 * Periodically emits deltas based off a Counters object.
 */
class CountersMonitor(counters: Counters) extends AbstractMonitor
{
  def doMonitor(emitter: ServiceEmitter) = {
    counters.snapshotAndReset() foreach emitter.emit
    true
  }
}
