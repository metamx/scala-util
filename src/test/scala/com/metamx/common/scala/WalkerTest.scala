package com.metamx.common.scala

import com.simple.simplespec.Matchers
import org.junit.Test

class WalkerTest extends Matchers
{

  def newWalker(): Walker[String] = {
    new Walker[String] {
      override def foreach(f: String => Unit) = List("hey", "there") foreach f
    }
  }

  @Test
  def testSimple()
  {
    val walker = newWalker()
    walker.toList must be(List("hey", "there"))
    walker.toList must be(List("hey", "there"))
    walker.foldLeft(0)(_ + _.size) must be(8)
    walker.foldLeft(0)(_ + _.size) must be(8)
  }

  @Test
  def testMap()
  {
    val walker = newWalker().map(_.size)
    walker.toList must be(List(3, 5))
    walker.toList must be(List(3, 5))
  }

  @Test
  def testFlatMap()
  {
    val walker = newWalker().flatMap(x => x)
    walker.toList must be(List('h', 'e', 'y', 't', 'h', 'e', 'r', 'e'))
    walker.toList must be(List('h', 'e', 'y', 't', 'h', 'e', 'r', 'e'))
  }

  @Test
  def testFilter()
  {
    val walker = newWalker().flatMap(x => x).filter(_ == 'e')
    walker.toList must be(List('e', 'e', 'e'))
    walker.toList must be(List('e', 'e', 'e'))
  }

  @Test
  def testPlusPlus()
  {
    val walker = newWalker() ++ newWalker().map(_.substring(0, 1))
    walker.toList must be(List("hey", "there", "h", "t"))
    walker.toList must be(List("hey", "there", "h", "t"))
  }

  @Test
  def testSize()
  {
    val walker = newWalker().flatMap(x => x)
    walker.size must be(8)
    walker.size must be(8)
  }

  @Test
  def testToSet()
  {
    val walker = newWalker().flatMap(x => x)
    walker.toSet must be(Set('h', 'e', 'y', 't', 'r'))
    walker.toSet must be(Set('h', 'e', 'y', 't', 'r'))
  }

  @Test
  def testFromIterable()
  {
    val walker = Walker(Seq(1, 2, 3, 3))
    walker.toSet must be(Set(1, 2, 3))
    walker.toSet must be(Set(1, 2, 3))
  }
}
