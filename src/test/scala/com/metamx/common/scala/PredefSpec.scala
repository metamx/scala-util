package com.metamx.common.scala

import org.junit.Test
import com.simple.simplespec.Spec
import com.metamx.common.scala.Predef._

class PredefSpec extends Spec {

  class A {

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

}
