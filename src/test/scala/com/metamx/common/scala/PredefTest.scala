package com.metamx.common.scala

import com.metamx.common.scala.Predef._
import com.simple.simplespec.Matchers
import org.junit.Test

class PredefTest extends Matchers {

  @Test def testRequiringTwoArgThrow() {
    val retval = try {
      1 requiring(_ > 3, "str")
    } catch {
      case e: Exception => 0
    }

    retval must be(0)
  }

  @Test def testRequiringTwoArgNoThrow() {
    val retval = try {
      5 requiring(_ > 3, "str")
    } catch {
      case e: Exception => 0
    }

    retval must be(5)
  }

}
