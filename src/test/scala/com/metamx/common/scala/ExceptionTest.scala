package com.metamx.common.scala

import com.metamx.common.scala.exception.{causeMatches, causedBy, causes}
import com.simple.simplespec.Matchers
import java.io.IOException
import org.junit.Test

class ExceptionTest extends Matchers {

  @Test
  def testCausesEmpty() {
    val e = new RuntimeException("foo")
    causes(e).toList.map(_.getMessage) must be(List("foo"))
  }

  @Test
  def testCausesSingle() {
    val e = new RuntimeException("foo", new RuntimeException("bar"))
    causes(e).toList.map(_.getMessage) must be(List("foo", "bar"))
  }

  @Test
  def testCausesMulti() {
    val e = new RuntimeException("foo", new RuntimeException("bar", new RuntimeException("baz")))
    causes(e).toList.map(_.getMessage) must be(List("foo", "bar", "baz"))
  }

  @Test
  def testCauseMatches() {
    val e = new UnsupportedOperationException("foo", new IOException("bar", new ArrayIndexOutOfBoundsException("baz")))
    causeMatches(e) { case _ => true } must be(true)
    causeMatches(e) { case _ => false } must be(false)
    causeMatches(e) { case _: RuntimeException => false } must be(false)
    causeMatches(e) { case _: IOException => true } must be(true)
    causeMatches(e) { case _: UnsupportedOperationException => false; case _: IOException => true } must be(true)
    causeMatches(e) { case x: IOException if x.getMessage == "rofl" => true } must be(false)
    causeMatches(e) { case _: ArrayIndexOutOfBoundsException => true } must be(true)
    causeMatches(e) { case _: NoSuchElementException => true } must be(false)
  }

  @Test
  def testCausedBy() {
    val e = new UnsupportedOperationException("foo", new IOException("bar", new ArrayIndexOutOfBoundsException("baz")))
    causedBy[RuntimeException](e) must be(true)
    causedBy[IOException](e) must be(true)
    causedBy[UnsupportedOperationException](e) must be(true)
    causedBy[ArrayIndexOutOfBoundsException](e) must be(true)
    causedBy[NoSuchElementException](e) must be(false)
  }

  @Test
  def testCausedByTryCatch() {
    val x = try {
      throw new UnsupportedOperationException("foo", new IOException("bar", new ArrayIndexOutOfBoundsException("baz")))
    } catch {
      case e if causedBy[ArrayIndexOutOfBoundsException](e) =>
        true
      case _ =>
        false
    }
    x must be(true)
  }

  @Test
  def testCausedByTryCatchNoMatch() {
    val x = try {
      throw new UnsupportedOperationException("foo", new IOException("bar", new ArrayIndexOutOfBoundsException("baz")))
    } catch {
      case e if causedBy[NoSuchElementException](e) =>
        true
      case _ =>
        false
    }
    x must be(false)
  }
}
