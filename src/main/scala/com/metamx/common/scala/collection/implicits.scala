package com.metamx.common.scala.collection

import scala.collection.{MapLike, TraversableLike}

object implicits
{
  implicit def TraversableOnceOps[X, F[Y] <: TraversableOnce[Y]](xs: F[X]) = new TraversableOnceOps[X,F](xs)
  implicit def TraversableLikeOps[X, F[Y] <: TraversableLike[Y, F[Y]]](xs: F[X]) = new TraversableLikeOps[X,F](xs)
  implicit def IteratorOps[X](xs: Iterator[X]) = new IteratorOps[X](xs)
  implicit def MapLikeOps[A, B, Repr <: MapLike[A, B, Repr] with scala.collection.Map[A, B]](m: MapLike[A, B, Repr]) = new MapLikeOps[A, B, Repr](m)
}
