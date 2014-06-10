package com.metamx.common.scala.net

import com.metamx.common.scala.net.uri._
import com.simple.simplespec.Matchers
import org.junit.Test
import scala.collection.immutable.ListMap

class UriTest extends Matchers
{

  @Test
  def testToQueryString()
  {
    Seq(("a", 2), ("a", 3), ("b", "foo")).toQueryString must be("a=2&a=3&b=foo")
    ListMap("a" -> 2, "b" -> "foo").toQueryString must be("a=2&b=foo")
  }

}
