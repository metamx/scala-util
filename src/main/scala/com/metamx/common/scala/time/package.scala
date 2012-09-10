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

package object time {

  import org.apache.commons.lang.time.StopWatch
  import org.joda.time.ReadableDateTime
  import org.joda.time.ReadableDuration
  import org.joda.time.ReadableInterval
  import org.joda.time.ReadablePeriod
  import org.scala_tools.time.Imports._

  def timed[X](f: => X): (Long, X) = {
    val timer = new StopWatch
    timer.start
    val x = f
    timer.stop
    (timer.getTime, x)
  }

  class DateTimeOps(t: ReadableDateTime) {

    def min(u: ReadableDateTime) = new DateTime(t.millis min u.millis)
    def max(u: ReadableDateTime) = new DateTime(t.millis max u.millis)

  }
  implicit def DateTimeOps(t: ReadableDateTime) = new DateTimeOps(t)

  class DurationOps(a: ReadableDuration) {

    def min(b: ReadableDuration) = new Duration(a.millis min b.millis)
    def max(b: ReadableDuration) = new Duration(a.millis max b.millis)

    def at    (t: DateTime) : Interval = t to t+a
    def until (t: DateTime) : Interval = t-a to t

    def isEmpty  = a.getMillis == 0
    def nonEmpty = !isEmpty

  }
  implicit def DurationOps(d: ReadableDuration) = new DurationOps(d)

  class PeriodOps(a: ReadablePeriod) {

    def at    (t: DateTime) : Interval = t to t+a
    def until (t: DateTime) : Interval = t-a to t

  }
  implicit def PeriodOps(p: ReadablePeriod) = new PeriodOps(p)

  class IntervalOps(a: ReadableInterval) {

    def isEmpty  = a.toDurationMillis == 0
    def nonEmpty = !isEmpty

  }
  implicit def IntervalOps(i: ReadableInterval) = new IntervalOps(i)

  implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.by(_.millis)
  implicit val durationOrdering: Ordering[Duration] = Ordering.by(_.millis)

}
