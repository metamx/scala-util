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

import org.scala_tools.time.Imports._
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.Vector
import scala.collection.IndexedSeqLike
import scala.collection.mutable.Builder
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Stack
import scala.util.control.Breaks

import com.metamx.common.scala.exception._
import com.metamx.common.scala.Predef._

// Property: Intervals.intervals are separated (i.e. disjoint and non-contiguous), ascending, and all nonempty

class Intervals private (self: Vector[Interval])
extends IndexedSeq[Interval] with IndexedSeqLike[Interval, Intervals] {
  // (Protected constructors are broken until >=2.9.0.1: https://issues.scala-lang.org/browse/SI-4128)

  Intervals.validate(self)

  // IndexedSeqLike
  override def apply(i: Int) = self.apply(i)
  override def length        = self.length
  override def newBuilder    = Intervals.newBuilder

  def duration = new Duration(self.map(_.millis).sum)

  def overlaps(interval: Interval) = self.find(_ overlaps interval).isDefined

  def overlaps(dt: DateTime) = self.find(_ contains dt).isDefined

  def latest(_duration: Duration) = new Intervals(Vector() ++ new ListBuffer[Interval].withEffect { results =>
    val breaks = new Breaks; import breaks.{breakable, break}
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
    val is = new Stack[Interval] pushAll self.reverseIterator // (Push reversed, pop in order)
    val js = that.iterator.buffered
    def i  = is.head
    def j  = js.head
    while (is.nonEmpty) {
      while (js.nonEmpty && (j isBefore i)) {
        js.next
      }
      if (js.isEmpty || (j isAfter i)) {
        results += i
        is.pop
      } else {
        val overlap = i overlap j ensuring (_ != null, "Expected overlap: %s, %s" format (i,j))
        val (a,b) = (i.start to overlap.start, overlap.end to i.end)
        is.pop
        if (a.millis > 0) results += a
        if (b.millis > 0) is.push(b)
      }
    }
  }.result)

  /** Encode Intervals into a String in a vaguely compact and safe fashion (only uses a limited set of characters).
    * The scheme:
    *
    * <ul>
    * <li>Write a version number and a string representation of the period.</li>
    * <li>Convert each interval to two longs: (1) periods since the end of the previous interval, or since the epoch
    *     if there is no previous interval; and (2) interval length, in periods.</li>
    * <li>Encode all longs using a variable-length encoding that is padded out to a multiple of 6 bits.</li>
    * <li>Encode the 6-bit chunks using the characters 0-9, a-z, A-Z, -, and _.</li>
    * </ul>
    *
    * Example: "I00_PT1H_wNDb323" -> Intervals(2012-01-01T00:00:00.000/2012-01-01T03:00:00.000,
    *                                          2012-01-01T05:00:00.000/2012-01-01T08:00:00.000)
    */
  def encode(period: Period): String = {
    // Split a long into 5-bit chunks and write them in little-endian order, adding a
    // sixth high bit to denote whether or not there are more chunks coming. Works best
    // for nonnegative longs.
    def enc(x: Long): Seq[Byte] = {
      val bytes = new ListBuffer[Byte]
      var scratch = x

      while(scratch != 0) {
        val next = if((scratch >>> 5) == 0) {
          scratch & 31L
        } else {
          (scratch & 31L) | 32L
        }

        bytes += (next match {
          case b if (0 to 9   contains b) => '0' + b
          case b if (10 to 35 contains b) => 'a' + (b - 10)
          case b if (36 to 61 contains b) => 'A' + (b - 36)
          case 62                         => '-'
          case 63                         => '_'
        }).byteValue

        scratch >>>= 5
      }

      if(bytes.nonEmpty) {
        bytes
      } else {
        Seq('0'.toByte)
      }
    }

    // How many periods are in some Interval?
    def periods(interval: Interval) = {
      // Can we convert period to a Duration?
      val durationOption = period.toStandardDuration swallow { case e: UnsupportedOperationException => }

      durationOption match {
        case Some(duration) => // Yes
          if(interval.millis % duration.millis == 0) {
            interval.millis / duration.millis
          } else {
            throw new IllegalArgumentException("Interval[%s] is not a multiple of period[%s]" format (interval, period))
          }

        case None => // No
          // Awful way of determining how many periods are in "interval".
          var dt = interval.start
          var nperiods = 0L
          while(dt < interval.end) {
            dt += period
            nperiods += 1
          }

          if(dt != interval.end) {
            throw new IllegalArgumentException("Interval[%s] is not a multiple of period[%s]" format (interval, period))
          }

          nperiods
      }
    }

    var epoch = new DateTime(0)
    val longs = self.flatMap { interval =>
      // Interval start, in periods since the end of the previous interval (or zero, if this is the first interval)
      val pStart =
        if(interval.start isAfter epoch) {
          periods(new Interval(epoch, interval.start))
        } else {
          periods(new Interval(interval.start, epoch)) * -1
        }

      // Interval duration, in periods
      val pDuration = periods(interval)

      // Update epoch
      epoch = interval.end

      Seq(pStart, pDuration)
    }

    "I00_%s_%s" format (period.toString, new String(longs.map(enc).flatten.toArray))
  }

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
    val ascending = intervals.toIndexedSeq sortBy ((i: Interval) => i.start.millis)
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

  /** Inverse of Intervals.encode. */
  def decode(string: String): Intervals = {
    def dec(xs: Iterable[Byte]): Seq[Long] = {
      var curValue: Long = 0L
      var curExpo: Long  = 0L
      val longs          = new ListBuffer[Long]

      for(x <- xs) {
        // accumulate x into curValue
        curValue += ((x & 31L) << (curExpo * 5L))
        if((x & 32L) == 0L) {
          // curValue is done
          longs += curValue
          curValue = 0L
          curExpo  = 0L
        } else {
          curExpo += 1
        }
      }

      longs.toSeq
    }

    val (period, bytes) = string.split("_", 3) match {
      case Array("I00", x, y) => (new Period(x), y.getBytes)
      case _                  => throw new IllegalArgumentException("Cannot decode Intervals from string: %s" format string)
    }

    val longs = dec(
      bytes.map(b => b match {
        case b if('0' to '9' map (_.toByte) contains b) => b - '0'.toByte
        case b if('a' to 'z' map (_.toByte) contains b) => b - 'a'.toByte + 10
        case b if('A' to 'Z' map (_.toByte) contains b) => b - 'A'.toByte + 36
        case '-'                                        => 62
        case '_'                                        => 63
      }).map(_.toByte)
    )

    // Try to convert period to a Duration
    val durationOption = period.toStandardDuration swallow { case e: UnsupportedOperationException => }
    def addPeriods(dt: DateTime, n: Long) = {
      durationOption match {
        case Some(duration) =>
          dt + new Period(duration.millis * n)
        case None =>
          (0L until n).foldLeft(dt) { (dt, _) => dt + period }
      }
    }

    var epoch = new DateTime(0)
    Intervals(
      (for(Seq(pStart, pDuration) <- longs.iterator.sliding(2, 2)) yield {
        val dtStart  = addPeriods(epoch,   pStart)
        val dtEnd    = addPeriods(dtStart, pDuration)
        val interval = new Interval(dtStart, dtEnd)
        epoch = interval.end
        interval
      }).toSeq
    )
  }

}
