package com.metamx.common.scala

import java.util.concurrent.atomic.AtomicBoolean
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

  def map[B](f: A => B): Walker[B] = new Walker[B] {
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

  def withFilter(p: A => Boolean): Walker[A] = filter(p)

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

  def size: Long = {
    var count = 0L
    foreach(_ => count += 1)
    count
  }
}

object Walker
{
  def empty[A]: Walker[A] = Walker[A](Nil)

  def apply[A](xs: Iterable[A]): Walker[A] = new Walker[A] {
    override def foreach(f: A => Unit) = xs foreach f
  }

  def apply[A](foreachFn: (A => Unit) => Unit): Walker[A] = new Walker[A] {
    override def foreach(f: A => Unit) {
      foreachFn(f)
    }
  }

  def once[A](foreachFn: (A => Unit) => Unit): Walker[A] = new Walker[A] {
    val finished = new AtomicBoolean(false)

    override def foreach(f: A => Unit) {
      val wasFinished = finished.getAndSet(true)
      if (wasFinished) {
        throw new IllegalStateException("Cannot walk more than once")
      } else {
        foreachFn(f)
      }
    }
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
