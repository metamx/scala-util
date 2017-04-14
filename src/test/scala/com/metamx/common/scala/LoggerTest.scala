package com.metamx.common.scala

import com.simple.simplespec.Matchers
import org.junit.Test

class LoggerTest extends Matchers
{
  @Test
  def testLogVariable() {
    val obj = new SimpleExample
    obj.getLog must beA[Logger]
  }

}

class SimpleExample extends Logging {
  def getLog = log
}
