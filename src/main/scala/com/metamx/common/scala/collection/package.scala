/*
 * Copyright 2012 Metamarkets Group Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metamx.common.scala

import com.metamx.common.scala.Predef.EffectOps
import scala.collection.generic.{CanBuildFrom => CBF}
import scala.collection.{MapLike, TraversableLike}

package object collection {

  /**
   * Build a stream by repeatedly evaluating xs until it's empty
   * Space safe: O(max xs.size) space
   */
  def untilEmpty[X](xs: => Iterable[X]): Stream[X] = (new Iterator[Iterator[X]] {
    var _xs = None : Option[Iterable[X]]
    def hasNext = (_xs orElse { Some(xs) withEffect { _xs = _ } }).get.nonEmpty
    def next    = (_xs orElse Some(xs) withEffect { _ => _xs = None }).get.iterator
  }: Iterator[Iterator[X]]).flatten.toStream

  class TraversableOnceOps[X, F[Y] <: TraversableOnce[Y]](xs: F[X]) {

    def ifEmpty    (f: => Any): F[X] = { if (xs.isEmpty)  f; xs }
    def ifNonEmpty (f: => Any): F[X] = { if (xs.nonEmpty) f; xs }

    def toMapOfSets[K, V](implicit ev: X <:< (K, V)): Map[K, Set[V]] = {
      for ((k, vs) <- xs.toSeq.groupBy(_._1)) yield {
        (k, vs.iterator.map(_._2).toSet)
      }
    }

    def toMapOfSeqs[K, V](implicit ev: X <:< (K, V)): Map[K, Seq[V]] = {
      for ((k, vs) <- xs.toSeq.groupBy(_._1)) yield {
        (k, vs.map(_._2))
      }
    }

    def onlyElement: X = {
      val iter = xs.toIterator
      if (!iter.hasNext) {
        throw new IllegalArgumentException("expected single element")
      }
      val elt = iter.next()
      if (iter.hasNext) {
        throw new IllegalArgumentException("expected single element")
      }
      elt
    }
  }
  implicit def TraversableOnceOps[X, F[Y] <: TraversableOnce[Y]](xs: F[X]) = new TraversableOnceOps[X,F](xs)

  class TraversableLikeOps[X, F[Y] <: TraversableLike[Y, F[Y]]](xs: F[X]) {

    /**
     * For preserving laziness, e.g.
     *
     *   Stream.fill(n) { ... } onComplete { ns => log.debug("Evaluation complete: %s", ns) }
     */
    def onComplete(f: F[X] => Unit)(implicit bf: CBF[F[X], X, F[X]]): F[X] = {
      xs ++ new Iterator[X] {
        def hasNext = { f(xs); false }
        def next    = throw new Exception("Unreachable")
      }
    }

    /**
     * Similar to takeWhile(!p), but include the last-tested element and don't evaluate beyond it:
     *
     *   Stream.from(0) map { x => assert(x < 3); x } takeUntil (_ == 2) toList  -->  List(0,1,2)
     *   Stream.from(0) map { x => assert(x < 3); x } takeWhile (_ < 3)  toList  -->  assertion error: 3 < 3
     */
    def takeUntil(p: X => Boolean)(implicit bf: CBF[F[X], X, F[X]]): F[X] = {
      object last { var x: X = _ } // (Can't use _ for local vars; stick it into a field of a local object instead)
      xs.takeWhile { x => last.x = x; !p(x) } ++ Iterator.fill(1) { last.x } // (Defer eval of last.x)
    }

  }
  implicit def TraversableLikeOps[X, F[Y] <: TraversableLike[Y, F[Y]]](xs: F[X]) = new TraversableLikeOps[X,F](xs)

  class MapLikeOps[A, +B, +Repr <: MapLike[A, B, Repr] with scala.collection.Map[A, B]](m: MapLike[A, B, Repr]) {

    def strictMapValues[C, That](f: B => C)(implicit bf: CBF[Repr, (A, C), That]): That = {
      m.map(kv => (kv._1, f(kv._2)))
    }

    def strictFilterKeys(f: A => Boolean): Repr = {
      m.filter(kv => f(kv._1))
    }

  }
  implicit def MapLikeOps[A, B, Repr <: MapLike[A, B, Repr] with scala.collection.Map[A, B]](m: MapLike[A, B, Repr]) = new MapLikeOps[A, B, Repr](m)

  // Mimic TravserableLikeOps for Iterator, which isn't TraversableLike
  class IteratorOps[X](xs: Iterator[X]) {

    def onComplete(f: Iterator[X] => Unit): Iterator[X] = xs ++ new Iterator[X] {
      def hasNext = { f(xs); false }
      def next    = throw new Exception("Unreachable")
    }

    def takeUntil(p: X => Boolean): Iterator[X] = {
      object last { var x: X = _ } // (Can't use _ for local vars; stick it into a field of a local object instead)
      xs.takeWhile { x => last.x = x; !p(x) } ++ Iterator.fill(1) { last.x }
    }

  }
  implicit def IteratorOps[X](xs: Iterator[X]) = new IteratorOps[X](xs)

}
