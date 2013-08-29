package com.metamx.common.scala.counters

import com.metamx.common.scala.event.Metric

/**
 * Snapshottable counters. Useful for both streaming metrics (snapshot periodically, emit) and batch
 * metrics (snapshot once at the end of a task).
 */
trait Counters
{
  def snapshotAndReset(): Counters.Snapshot

  def monitor = new CountersMonitor(this)
}

object Counters
{
  type Snapshot = Seq[Metric]
}
