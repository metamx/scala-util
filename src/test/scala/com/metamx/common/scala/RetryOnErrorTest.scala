package com.metamx.common.scala

import com.metamx.common.scala.control._
import com.simple.simplespec.Matchers
import org.junit.Test
import org.scala_tools.time.Imports._

class RetryOnErrorTest extends Matchers
{

  def transientFailure[E <: Exception](period: Period)(implicit cm: ClassManifest[E]) = {
    val end = DateTime.now + period
    () => if (DateTime.now >= end) "hello world" else throw cm.erasure.newInstance().asInstanceOf[E]
  }

  @Test
  def testSimpleOneArg()
  {
    val f = transientFailure[IllegalStateException](1.second)
    retryOnError((e: IllegalStateException) => true) { f.apply() } must be("hello world")
  }

  @Test
  def testSimpleOneArgAltForm()
  {
    val f = transientFailure[IllegalStateException](1.second)
    retryOnError[IllegalStateException](_ => true) { f() } must be("hello world")
  }

  @Test
  def testWrongExceptionOneArg()
  {
    val f = transientFailure[IllegalStateException](1.second)
    evaluating {
      retryOnError((e: IllegalArgumentException) => true)(f())
    } must throwAn[IllegalStateException]
  }

  @Test
  def testSimpleTwoArg()
  {
    val f = transientFailure[IllegalStateException](1.second)
    retryOnErrors(
      ifException[IllegalArgumentException],
      ifException[IllegalStateException]
    )(f()) must be("hello world")
  }

  @Test
  def testWrongExceptionTwoArg()
  {
    val f = transientFailure[IllegalStateException](1.second)
    evaluating {
      retryOnErrors(
        ifException[IllegalArgumentException],
        ifException[NumberFormatException]
      )(f())
    } must throwAn[IllegalStateException]
  }

  @Test
  def testTimeoutNotReached()
  {
    val f = transientFailure[IllegalStateException](1.second)
    retryOnError(
      ifException[IllegalStateException] untilPeriod(3.seconds)
    )(f()) must be("hello world")
  }

  @Test
  def testTimeoutReached()
  {
    val f = transientFailure[IllegalStateException](10.seconds)
    evaluating {
      retryOnError(
        ifException[IllegalStateException] untilPeriod(500.millis)
      )(f())
    } must throwAn[IllegalStateException]
  }

  @Test
  def testCountoutNotReached()
  {
    val f = transientFailure[IllegalStateException](1.second)
    retryOnError(
      ifException[IllegalStateException] untilCount(30)
    )(f()) must be("hello world")
  }

  @Test
  def testCountoutReached()
  {
    var count = 0
    val f = transientFailure[IllegalStateException](1.second)
    evaluating {
      retryOnError(
        ifException[IllegalStateException] untilCount(1)
      ) {
        count += 1
        f()
      }
    } must throwAn[IllegalStateException]
    count must be(2)
  }


  @Test
  def testCountoutZero()
  {
    var count = 0
    val f = transientFailure[IllegalStateException](1.second)
    evaluating {
      retryOnError(
        ifException[IllegalStateException] untilCount(0)
      ) {
        count += 1
        f()
      }
    } must throwAn[IllegalStateException]
    count must be(1)
  }

  @Test
  def testIfExceptionSatisfies()
  {
    val f = transientFailure[IllegalStateException](1.second)
    retryOnErrors(ifExceptionSatisfies[IllegalStateException](_.getCause == null)) { f.apply() } must be("hello world")
  }

  @Test
  def testIfExceptionDoesNotSatisfyClass()
  {
    val f = transientFailure[IllegalStateException](1.second)
    evaluating {
      retryOnErrors(ifExceptionSatisfies[IllegalArgumentException](_.getCause == null)) { f.apply() }
    } must throwAn[IllegalStateException]
  }

  @Test
  def testIfExceptionDoesNotSatisfyTest()
  {
    val f = transientFailure[IllegalStateException](1.second)
    evaluating {
      retryOnErrors(ifExceptionSatisfies[IllegalStateException](_.getCause != null)) { f.apply() }
    } must throwAn[IllegalStateException]
  }

}
