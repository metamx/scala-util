package com.metamx.common.scala

import org.joda.time.DateTime

object timekeeper {
  /**
   * Timekeepers are a more testable alternative to `DateTime.now` or `new DateTime()`. Most applications will use
   * SystemTimekeeper in production and TestingTimekeeper in time-sensitive unit tests.
   */
  trait Timekeeper
  {
    def now: DateTime
  }

  class SystemTimekeeper extends Timekeeper with Serializable
  {
    def now = new DateTime()
  }

  class TestingTimekeeper extends Timekeeper
  {
    @volatile private[this] var _now: Option[DateTime] = None

    def now = _now getOrElse {
      throw new IllegalStateException("Time not set!")
    }

    def now_=(dt: DateTime) {
      _now = Some(dt)
    }
  }
}
