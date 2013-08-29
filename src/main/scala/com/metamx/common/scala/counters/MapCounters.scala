package com.metamx.common.scala.counters

import com.metamx.common.scala.collection.mutable.ConcurrentMap
import java.util.concurrent.atomic.AtomicLong
import com.metamx.common.scala.event.Metric

/**
 * Use this when too lazy to create a domain-specific Counters class. Uses memory linear in the number of keys
 * ever seen.
 */
class MapCounters(prototype: Metric) extends Counters
{
  private[this] val counters = ConcurrentMap[String, AtomicLong]()

  def increment(key: String) {
    add(key, 1)
  }

  def add(key: String, value: Long) {
    counters.putIfAbsent(key, new AtomicLong(0))
    counters(key).addAndGet(value)
  }

  def snapshotAndReset() = {
    (counters.keys map {
      k =>
        Metric(k, counters(k).getAndSet(0)) + prototype
    }).toIndexedSeq
  }
}
