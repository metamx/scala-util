package com.metamx.common.scala

import scala.collection.GenTraversableOnce

/**
 * Walkers provide a mechanism for walking through an underlying listish thing, exposed as a "foreach" method. They
 * do not return iterators, nor do they allow random access. This allows them to guarantee post-iteration cleanup
 * actions on the underlying resource, which will occur even if exceptions are thrown while walking.
 *
 * Walkers can be constructed such that the "foreach" method can only be called once. In that case, subsequent calls
 * should throw an IllegalStateException. Walkers can also be constructed with "foreach" methods that can be called
 * multiple times. In that case, each run should create and then clean up the resource-- saving state across runs is
 * usually counterproductive.
 */
trait Walker[+A]
{
  self =>

  def foreach(f: A => Unit)

  def map[B](f: A => B) = new Walker[B] {
    override def foreach(g: B => Unit) = self.foreach(a => g(f(a)))
  }

  def flatMap[B](f: A => GenTraversableOnce[B]): Walker[B] = new Walker[B] {
    override def foreach(g: B => Unit) = self.foreach(a => f(a) foreach g)
  }

  def filter(p: A => Boolean): Walker[A] = new Walker[A] {
    override def foreach(g: A => Unit) = self foreach {
      a =>
        if (p(a)) {
          g(a)
        }
    }
  }

  def ++[B >: A](other: Walker[B]): Walker[B] = new Walker[B] {
    override def foreach(g: B => Unit) {
      self foreach g
      other foreach g
    }
  }

  def toList: List[A] = {
    val builder = List.newBuilder[A]
    foreach(builder += _)
    builder.result()
  }

  def toSet[B >: A]: Set[B] = {
    val builder = Set.newBuilder[B]
    foreach(builder += _)
    builder.result()
  }

  def foldLeft[B](zero: B)(combine: (B, A) => B): B = {
    var current = zero
    foreach {
      a =>
        current = combine(current, a)
    }
    current
  }

  def size = foldLeft(0)((i, _) => i + 1)
}

object Walker
{
  def empty[A] = Walker[A](Nil)

  def apply[A](xs: Iterable[A]): Walker[A] = new Walker[A] {
    override def foreach(f: A => Unit) = xs foreach f
  }

  class WalkerTuple2Ops[A, B](walker: Walker[(A, B)])
  {
    def toMap: Map[A, B] = {
      val builder = Map.newBuilder[A, B]
      walker.foreach(builder += _)
      builder.result()
    }
  }
  implicit def WalkerTuple2Ops[A, B](walker: Walker[(A, B)]) = new WalkerTuple2Ops(walker)
}
