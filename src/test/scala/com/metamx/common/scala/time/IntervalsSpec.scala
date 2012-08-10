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

import com.codahale.simplespec.Spec
import org.junit.Test
import org.scala_tools.time.Imports._

import com.metamx.common.scala.junit.contextually

class IntervalsSpec extends Spec {

  def D(x: Long)          : DateTime = new DateTime(x)
  def I(x: Long, y: Long) : Interval = new Interval(D(x), D(y))
  def I(xy: (_,_))        : Interval = I(xy._1.asInstanceOf[Number].longValue, xy._2.asInstanceOf[Number].longValue)

  class A {

    @Test def `validate ensures intervals are separated` {
      evaluating {
        Intervals.validate(
          Seq(I(0,1), I(0,1))
        )
      } must throwAn[AssertionError]("assertion failed: Intervals must be separated: .*".r)
      evaluating {
        Intervals.validate(
          Seq(I(0,1), I(1,2))
        )
      } must throwAn[AssertionError]("assertion failed: Intervals must be separated: .*".r)
      evaluating {
        Intervals.validate(
          Seq(I(0,2), I(1,3))
        )
      } must throwAn[AssertionError]("assertion failed: Intervals must be separated: .*".r)
    }

    @Test def `validate ensures intervals are ascending` {
      evaluating {
        Intervals.validate(
          Seq(I(2,3), I(0,1))
        )
      } must throwAn[AssertionError]("assertion failed: Intervals must be ascending: .*".r)
    }

    @Test def `validate ensures intervals are all nonempty` {
      evaluating {
        Intervals.validate(
          Seq(I(0,0))
        )
      } must throwAn[AssertionError]("assertion failed: Intervals must be all nonempty: .*".r)
    }

    @Test def union {
      Seq(

        Seq((0,1),  (5,10)) -> Seq((0,1), (5,10)),
        Seq((0,5),  (5,10)) -> Seq((0,10)),
        Seq((0,10), (5,10)) -> Seq((0,10)),
        Seq((0,10), (5,10)) -> Seq((0,10)),

        Seq((7,8), (5,7), (9,9), (2,3), (2,6), (0,1)) -> Seq((0,1), (2,8)),

        Seq() -> Seq()

      ) foreach { contextually { case (from, to) =>
        Intervals.union(from map I).toSeq must be(to map I)
        Intervals.union(from map I).toSeq must be(Intervals(to map I))
        Intervals(from map I) must be(Intervals(to map I))
      }}
    }

    @Test def map {
      val xs = Intervals(I(0,1), I(1,2), I(3,4), I(7,8), I(9,10))
      xs map { i => I(i.start.millis + 1, i.end.millis + 1) } must be(Intervals(I(1,3), I(4,5), I(8,9), I(10,11)))
      xs map { i => I(i.start.millis / 2, i.end.millis / 2) } must be(Intervals(I(0,2), I(3,5)))
    }

    @Test def filter {
      val xs = Intervals(I(0,1), I(1,2), I(3,4), I(7,8), I(9,10))
      xs filter { i => i.end <  D(5) } must be(Intervals(I(0,2), I(3,4)))
      xs filter { i => i.end >= D(5) } must be(Intervals(I(7,8), I(9,10)))
      xs filter { i => i.millis >  1 } must be(Intervals(I(0,2)))
      xs filter { i => i.millis <= 1 } must be(Intervals(I(3,4), I(7,8), I(9,10)))
    }

    @Test def ++ {
      Intervals(I(0,1), I(3,4)) ++ Intervals(I(4,5), I(6,10)) must be(Intervals(I(0,1), I(3,5), I(6,10)))
    }

    @Test def groupBy {
      val xs = Intervals(I(0,2), I(3,4), I(7,8), I(9,15), I(20,22), I(23,24))
      xs groupBy (_.duration) must be(Map(
        new Duration(1) -> Intervals(I(3,4), I(7,8), I(23,24)),
        new Duration(2) -> Intervals(I(0,2), I(20,22)),
        new Duration(6) -> Intervals(I(9,15))
      ))
    }

    @Test def duration {
      Intervals(I(0,2), I(3,4), I(7,8), I(9,10)) .duration must be(new Duration(5))
      Intervals(I(3,4), I(7,8), I(9,10))         .duration must be(new Duration(3))
      Intervals(I(-10,10))                       .duration must be(new Duration(20))
      Intervals()                                .duration must be(new Duration(0))
    }

    @Test def latest {
      Seq(

        (Seq((0,10)), 0)  -> Seq(),
        (Seq((0,10)), 3)  -> Seq((7,10)),
        (Seq((0,10)), 10) -> Seq((0,10)),
        (Seq((0,10)), 13) -> Seq((0,10)),

        (Seq((2,4), (6,7), (8,10)), 0)  -> Seq(),
        (Seq((2,4), (6,7), (8,10)), 1)  -> Seq((9,10)),
        (Seq((2,4), (6,7), (8,10)), 2)  -> Seq((8,10)),
        (Seq((2,4), (6,7), (8,10)), 3)  -> Seq((6,7), (8,10)),
        (Seq((2,4), (6,7), (8,10)), 4)  -> Seq((3,4), (6,7), (8,10)),
        (Seq((2,4), (6,7), (8,10)), 5)  -> Seq((2,4), (6,7), (8,10)),
        (Seq((2,4), (6,7), (8,10)), 6)  -> Seq((2,4), (6,7), (8,10)),
        (Seq((2,4), (6,7), (8,10)), 10) -> Seq((2,4), (6,7), (8,10)),

        (Seq(), 3) -> Seq()

      ) foreach { contextually { case ((from, duration), to) =>
        Intervals(from map I).latest(new Duration(duration)) must be(Intervals(to map I))
      }}
    }

    @Test def -- {
      Seq(

        (Seq((0,10)), Seq((4,5)))   -> Seq((0,4), (5,10)),
        (Seq((0,10)), Seq((1,9)))   -> Seq((0,1), (9,10)),
        (Seq((0,10)), Seq((0,10)))  -> Seq(),
        (Seq((0,10)), Seq((-1,11))) -> Seq(),
        (Seq((0,10)), Seq((0,5)))   -> Seq((5,10)),
        (Seq((0,10)), Seq((-5,5)))  -> Seq((5,10)),
        (Seq((0,10)), Seq((5,10)))  -> Seq((0,5)),
        (Seq((0,10)), Seq((5,15)))  -> Seq((0,5)),

        (Seq((0,10)), Seq((1,2), (3,6), (7,9)))                  -> Seq((0,1), (2,3), (6,7), (9,10)),
        (Seq((0,10)), Seq((-1,0), (1,2), (3,6), (7,9), (10,11))) -> Seq((0,1), (2,3), (6,7), (9,10)),
        (Seq((0,10)), Seq((-1,2), (3,6), (7,11)))                -> Seq((2,3), (6,7)),
        (Seq((0,10)), Seq((-1,6), (7,11)))                       -> Seq((6,7)),
        (Seq((0,10)), Seq((-1,2), (3,11)))                       -> Seq((2,3)),
        (Seq((0,10)), Seq((-1,11)))                              -> Seq(),

        (Seq((0,3), (4,6), (8,10)), Seq((0,1)))  -> Seq((1,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,2)))  -> Seq((2,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,3)))  -> Seq((4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,4)))  -> Seq((4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,5)))  -> Seq((5,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,6)))  -> Seq((8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,7)))  -> Seq((8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,8)))  -> Seq((8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,9)))  -> Seq((9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((0,10))) -> Seq(),

        (Seq((0,3), (4,6), (8,10)), Seq((1,2)))  -> Seq((0,1), (2,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,3)))  -> Seq((0,1), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,4)))  -> Seq((0,1), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,5)))  -> Seq((0,1), (5,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,6)))  -> Seq((0,1), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,7)))  -> Seq((0,1), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,8)))  -> Seq((0,1), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,9)))  -> Seq((0,1), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((1,10))) -> Seq((0,1)),

        (Seq((0,3), (4,6), (8,10)), Seq((2,3)))  -> Seq((0,2), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,4)))  -> Seq((0,2), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,5)))  -> Seq((0,2), (5,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,6)))  -> Seq((0,2), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,7)))  -> Seq((0,2), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,8)))  -> Seq((0,2), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,9)))  -> Seq((0,2), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((2,10))) -> Seq((0,2)),

        (Seq((0,3), (4,6), (8,10)), Seq((3,4)))  -> Seq((0,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((3,5)))  -> Seq((0,3), (5,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((3,6)))  -> Seq((0,3), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((3,7)))  -> Seq((0,3), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((3,8)))  -> Seq((0,3), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((3,9)))  -> Seq((0,3), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((3,10))) -> Seq((0,3)),

        (Seq((0,3), (4,6), (8,10)), Seq((4,5)))  -> Seq((0,3), (5,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((4,6)))  -> Seq((0,3), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((4,7)))  -> Seq((0,3), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((4,8)))  -> Seq((0,3), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((4,9)))  -> Seq((0,3), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((4,10))) -> Seq((0,3)),

        (Seq((0,3), (4,6), (8,10)), Seq((5,6)))  -> Seq((0,3), (4,5), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((5,7)))  -> Seq((0,3), (4,5), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((5,8)))  -> Seq((0,3), (4,5), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((5,9)))  -> Seq((0,3), (4,5), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((5,10))) -> Seq((0,3), (4,5)),

        (Seq((0,3), (4,6), (8,10)), Seq((6,7)))  -> Seq((0,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((6,8)))  -> Seq((0,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((6,9)))  -> Seq((0,3), (4,6), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((6,10))) -> Seq((0,3), (4,6)),

        (Seq((0,3), (4,6), (8,10)), Seq((7,8)))  -> Seq((0,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((7,9)))  -> Seq((0,3), (4,6), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((7,10))) -> Seq((0,3), (4,6)),

        (Seq((0,3), (4,6), (8,10)), Seq((8,9)))  -> Seq((0,3), (4,6), (9,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((8,10))) -> Seq((0,3), (4,6)),

        (Seq((0,3), (4,6), (8,10)), Seq((9,10))) -> Seq((0,3), (4,6), (8,9)),

        (Seq((0,3), (4,6), (8,10)), Seq((9,11)))  -> Seq((0,3), (4,6), (8,9)),
        (Seq((0,3), (4,6), (8,10)), Seq((10,11))) -> Seq((0,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((-1,1)))  -> Seq((1,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((-1,0)))  -> Seq((0,3), (4,6), (8,10)),
        (Seq((0,3), (4,6), (8,10)), Seq((-1,11))) -> Seq(),

        (Seq((0,3), (4,7), (8,10)), Seq((0,3), (4,7), (8,10))) -> Seq(),
        (Seq((0,3), (4,7), (8,10)), Seq((0,3), (8,10)))        -> Seq((4,7)),
        (Seq((0,3), (4,7), (8,10)), Seq((1,2), (5,6)))         -> Seq((0,1), (2,3), (4,5), (6,7), (8,10)),
        (Seq((0,3), (4,7), (8,10)), Seq((1,2), (5,6), (7,9)))  -> Seq((0,1), (2,3), (4,5), (6,7), (9,10)),
        (Seq((0,3), (4,7), (8,10)), Seq((1,2), (5,6), (7,10))) -> Seq((0,1), (2,3), (4,5), (6,7)),
        (Seq((0,3), (4,7), (8,10)), Seq((1,2), (5,8)))         -> Seq((0,1), (2,3), (4,5), (8,10)),
        (Seq((0,3), (4,7), (8,10)), Seq((1,2), (5,9)))         -> Seq((0,1), (2,3), (4,5), (9,10)),
        (Seq((0,3), (4,7), (8,10)), Seq((1,2), (5,10)))        -> Seq((0,1), (2,3), (4,5)),

        (Seq((0,3), (4,7), (8,10)), (0 to 8 by 2).map(i => (i,i+1))) -> Seq((1,2), (5,6), (9,10)),
        (Seq((0,3), (4,7), (8,10)), (1 to 9 by 2).map(i => (i,i+1))) -> Seq((0,1), (2,3), (4,5), (6,7), (8,9)),

        ((0 to 8 by 2).map(i => (i,i+1)), (0 to 8 by 2).map(i => (i,i+1))) -> Seq(),
        ((0 to 8 by 2).map(i => (i,i+1)), (1 to 9 by 2).map(i => (i,i+1))) -> (0 to 8 by 2).map(i => (i,i+1)),

        (Seq(), Seq()) -> Seq()

      ) foreach { contextually { case ((from, less), to) =>
        Intervals(from map I) -- (less map I) must be(Intervals(to map I))
      }}
    }

    @Test def encode_decode {
      def II(xs: String*) = Intervals(xs map (new Interval(_)))

      // Every other day in 2012 (2012-01/P1D, 2012-03/P1D, etc)
      val everyOtherDay = Intervals(
        (0 until 365 by 2) map (i => new DateTime("2012") + i.days) map (new Interval(_, 1.days))
      )

      val businessDays = Intervals(
        (0 until 365) map (i => new DateTime("2012") + i.days) filter (x => x.dayOfWeek.get >= 1 && x.dayOfWeek.get <= 5) map (new Interval(_, 1.days))
      )

      val oks = Seq(
        ("I00_P1D_I_e"  + (0 until 365).map(x => "1").mkString, everyOtherDay,                        "P1D"),
        ("I00_P1D_J_e5" + (0 until 51).map(x => "25").mkString, businessDays,                         "P1D"),
        ("I00_P1D_UGxG________fKbJbJb",                        II("0/1", "2/3"),                      "P1D"),
        ("I00_PT1H_wNDb323",                                   II("2012/2012T03", "2012T05/2012T08"), "PT1H"),
        ("I00_P1M_Ic121",                                      II("2003/P1M", "2003-04/P1M"),         "P1M"),
        ("I00_P1D_RUbvX1u",                                    II("2003/P1M", "2003-04/P1M"),         "P1D"),
        ("I00_PT1H_ULW8EnEI1Mm",                               II("2003/P1M", "2003-04/P1M"),         "PT1H"),
        ("I00_PT0.001S_wwzPMXFuwwFGWL2wwJKZT4wwOZDJ2",         II("2003/P1M", "2003-04/P1M"),         "PT0.001S"),
        ("I00_P1D_0Jb",                                        II("1970/1971"),                       "P1D"),
        ("I00_P1D_PQ__________fWm",                            II("1969/1971"),                       "P1D"),
        ("I00_P1D_PQ__________fJb",                            II("1969/1970"),                       "P1D"),
        ("I00_P1D_PQ__________fJbJbJb",                        II("1969/1970", "1971/1972"),          "P1D")
      )

      for((x, intervals, z) <- oks) {
        val period    = new Period(z)

        intervals.encode(period) must be(x)
        Intervals.decode(x) must be(intervals)
      }

      // Some errors
      evaluating {
        Intervals(
          Seq("2012-01-01/2012-01-01T03", "2012-01-01T05/2012-01-01T08") map (new Interval(_))
        ).encode(new Period("P1D"))
      } must throwAn[IllegalArgumentException]("""not a multiple of period\[P1D\]""".r)

      evaluating {
        Intervals(
          Seq("2012-01-01/2012-01-01T03", "2012-01-01T05/2012-01-01T08") map (new Interval(_))
        ).encode(new Period("P1M"))
      } must throwAn[IllegalArgumentException]("""not a multiple of period\[P1M\]""".r)

      evaluating {
        Intervals.decode("XXX")
      } must throwAn[IllegalArgumentException]("Cannot decode Intervals from string".r)
    }

  }

}
