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

package com.metamx.common.scala.time

import com.github.nscala_time.time.Imports._
import com.metamx.common.scala.Predef._
import scala.collection.IndexedSeqLike
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.Vector
import scala.collection.mutable.{Builder, ListBuffer}
import scala.util.control.Breaks

// Property: Intervals.intervals are separated (i.e. disjoint and non-contiguous), ascending, and all nonempty

class Intervals private (self: Vector[Interval])
extends IndexedSeq[Interval] with IndexedSeqLike[Interval, Intervals] with Serializable {

  Intervals.validate(self)

  // IndexedSeqLike
  override def apply(i: Int) = self.apply(i)
  override def length        = self.length
  override def newBuilder    = Intervals.newBuilder

  def duration = new Duration(self.map(_.millis).sum)

  def overlaps(interval: Interval) = self.exists(_ overlaps interval)

  def overlaps(dt: DateTime) = self.exists(_ contains dt)

  def earliest(_duration: Duration) = new Intervals(Vector() ++ new ListBuffer[Interval].withEffect { results =>
    val breaks = new Breaks; import breaks.{break, breakable}
    var duration = _duration
    breakable {
      for (i <- self.iterator) {
        if (duration.millis == 0) {
          break
        } else {
          results += new Interval(i.start, Seq(i.end, i.start + duration).min) withEffect { result =>
            duration -= result.duration
          }
        }
      }
    }
  })

  def latest(_duration: Duration) = new Intervals(Vector() ++ new ListBuffer[Interval].withEffect { results =>
    val breaks = new Breaks; import breaks.{break, breakable}
    var duration = _duration
    breakable {
      for (i <- self.reverseIterator) {
        if (duration.millis == 0) {
          break
        } else {
          results.insert(0, new Interval(i.start max (i.end - duration), i.end) withEffect { result =>
            duration -= result.duration
          })
        }
      }
    }
  })

  def -- (that: Iterable[Interval]) = new Intervals(Vector.newBuilder[Interval].withEffect { results =>
    var is = self
    val js = that.iterator.buffered
    def i  = is.head
    def j  = js.head
    while (is.nonEmpty) {
      while (js.nonEmpty && (j isBefore i)) {
        js.next
      }
      if (js.isEmpty || (j isAfter i)) {
        results += i
        is = is.tail
      } else {
        val overlap = i overlap j ensuring (_ != null, "Expected overlap: %s, %s" format (i,j))
        val (a, b) = (i.start to overlap.start, overlap.end to i.end)
        is = is.tail
        if (a.millis > 0) results += a
        if (b.millis > 0) is = b +: is
      }
    }
  }.result)

}

// Companion object in the style of BitSet, which we take as the canonical 0-type-arg collection type
object Intervals {

  // Factory methods
  val empty                                : Intervals = apply()
  def apply(is: Interval*)                 : Intervals = apply(is)
  def apply(is: TraversableOnce[Interval]) : Intervals = newBuilder.withEffect { b => is foreach (b += _) }.result

  // An efficient builder in terms of union (loglinear time, linear space)
  implicit def canBuildFrom = new CanBuildFrom[Intervals, Interval, Intervals] {
    def apply(from: Intervals) = newBuilder
    def apply()                = newBuilder
  }
  def newBuilder: Builder[Interval, Intervals] = Vector.newBuilder mapResult { xs =>
    new Intervals(union(xs))
  }

  // Union a sequence of intervals so that they satisfy: separated, ascending, all nonempty.
  // Loglinear time, linear space.
  def union(intervals: Iterable[Interval]) = Vector[Interval]() ++ {
    // Sort input intervals by start time and build up result incrementally
    val separated = new ListBuffer[Interval]
    var current   = None : Option[Interval]
    val ascending = intervals.toIndexedSeq sortBy ((i: Interval) => i.start.getMillis)
    for (i <- ascending; if i.millis > 0) {
      current match {
        case None                         => current = Some(i)
        case Some(j) if (j gap i) == null => current = Some(j.start to (j.end max i.end))
        case Some(j)                      => current = Some(i); separated += j
      }
    }
    current foreach { separated += _ }
    separated
  }

  // Ensure that intervals satisfy: separated, ascending, all nonempty
  def validate(intervals: Iterable[Interval]) = intervals withEffect { _ =>
    var prev = None : Option[Interval]
    for (b <- intervals) {
      assert(b.millis > 0, "Intervals must be all nonempty: %s" format b)
      for (a <- prev) {
        assert((a gap b) != null, "Intervals must be separated: %s, %s" format (a,b))
        assert(a isBefore b,      "Intervals must be ascending: %s, %s" format (a,b))
      }
      prev = Some(b)
    }
  }

}
