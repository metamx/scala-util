package com.metamx.common.scala.counters

import com.metamx.common.scala.counters.Counters.Snapshot
import com.metamx.common.scala.event.Metric
import scala.collection.mutable

/**
  * Use this when too lazy to create a domain-specific Counters class.
  * Aggregates values per each metric & dimensions pair.
  */
class NumericCounters[A](converter: (Numeric[A], A) => Number)(implicit numeric: Numeric[A]) extends Counters
{
  private[this] val lock = new AnyRef
  private[this] val map = mutable.Map[(String, Map[String, Iterable[String]]), A]()

  def inc(metric: String, dims: Map[String, Iterable[String]]) {
    add(metric, dims, numeric.one)
  }

  def add(metric: String, dims: Map[String, Iterable[String]], value: A) {
    lock.synchronized {
      val key = metric -> dims
      val curr = map.get(key) match {
        case Some(x) => x
        case None => numeric.zero
      }
      val updated = numeric.plus(curr, value)
      map.put(key, updated)
    }
  }

  def del(metric: String, dims: Map[String, Iterable[String]]) {
    lock.synchronized {
      val key = metric -> dims
      map.remove(key)
    }
  }

  override def snapshotAndReset(): Snapshot = {
    lock.synchronized {
      val snapshot = map.map {
        case ((metric, dims), value) =>
          new Metric(
            metric = metric,
            value = converter(numeric, value),
            userDims = dims,
            created = null
          )
      }.toList
      map.clear()
      snapshot
    }
  }
}

class LongCounters extends NumericCounters[Long](converter = (numeric, x) => numeric.toLong(x))

class DoubleCounters extends NumericCounters[Double](converter = (numeric, x) => numeric.toDouble(x))
