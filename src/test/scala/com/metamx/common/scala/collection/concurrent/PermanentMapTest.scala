package com.metamx.common.scala.collection.concurrent

import com.simple.simplespec.Matchers
import org.junit.Test

class PermanentMapTest extends Matchers
{

  @Test
  def testSimple()
  {
    // Not testing the concurrency parts, just basic functionality from a single thread's perspective.
    val m = new PermanentMap[String, Int]
    m.getOrElseUpdate("foo", 3) must be(3)
    m.getOrElseUpdate("foo", 5) must be(3)
    m.getOrElseUpdate("bar", 5) must be(5)
    m.get("bar") must be(Some(5))
    m.get("baz") must be(None)
  }

}
