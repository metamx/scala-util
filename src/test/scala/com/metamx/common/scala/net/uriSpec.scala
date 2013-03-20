package com.metamx.common.scala.net

import com.metamx.common.scala.net.uri._
import org.junit.Test
import com.simple.simplespec.Spec
import scala.collection.immutable.ListMap

class uriSpec extends Spec
{

  class A
  {
    @Test
    def testToQueryString()
    {
      Seq(("a", 2), ("a", 3), ("b", "foo")).toQueryString must be("a=2&a=3&b=foo")
      ListMap("a" -> 2, "b" -> "foo").toQueryString must be("a=2&b=foo")
    }
  }

}
