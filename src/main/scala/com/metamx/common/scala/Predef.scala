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

object Predef {

  def forever(f: => Unit) {
    while (true) {
      f
    }
  }

  // `x into f into g == g(f(x))', like F#'s pipeline operator:
  //    http://debasishg.blogspot.com/2009/09/thrush-combinator-in-scala.html
  //    http://en.wikibooks.org/wiki/F_Sharp_Programming/Higher_Order_Functions#The_.7C.3E_Operator
  class IntoOps[X](x: X) {
    def into[Y](f: X => Y): Y = f(x)
  }
  implicit def IntoOps[X](x: X) = new IntoOps(x)

  class NullOps[X](x: X) {
    def mapNull               (x0 : => X)   : X = if (x == null) x0   else x
    def mapNonNull[Y >: Null] (f  : X => Y) : Y = if (x == null) null else f(x)
    @deprecated("Use mapNull",    "0.5.0") def ifNull                (x0 : => X)   : X = mapNull(x0)
    @deprecated("Use mapNonNull", "0.5.0") def unlessNull[Y >: Null] (f  : X => Y) : Y = mapNonNull(f)
  }
  implicit def NullOps[X](x: X) = new NullOps(x)

  class EffectOps[X](x: X) {
    def withEffect(f: X => Unit): X = { f(x); x }
  }
  implicit def EffectOps[X](x: X) = new EffectOps(x)


  class RequiringOps[X](x: X){
    def requiring(f: X => (Boolean, String)) : X = { val (y, msg) = f(x); require(y, msg); x }
    def requiring(f: X => Boolean, message: => Any): X = { require(f(x), message); x }
  }
  implicit def RequiringOps[X](x: X) = new RequiringOps(x)

  class FinallyOps[X](x: X) {
    def withFinally[Y](close: X => Unit) = (f: X => Y) => try f(x) finally close(x)
  }
  implicit def FinallyOps[X](x: X) = new FinallyOps(x)

  class BooleanOps(x: Boolean) {
    def orElse  (f: => Unit): Boolean = { if (!x) f; x }
    def andThen (f: => Unit): Boolean = { if (x) f;  x }
  }
  implicit def BooleanOps(x: Boolean) = new BooleanOps(x)

  class TraversableOnceBooleanOps(xs: TraversableOnce[Boolean]) {
    def any: Boolean = xs exists identity
    def all: Boolean = xs forall identity
  }
  implicit def TraversableOnceBooleanOps(xs: TraversableOnce[Boolean]) = new TraversableOnceBooleanOps(xs)

  // Disjunction types à la Miles Sabin (http://stackoverflow.com/a/6312508/397334), e.g.
  //
  //   def size[X : (Int Or String)#F](x: X): Int = x match {
  //     case n: Int    => n
  //     case s: String => s.length
  //   }
  //
  type Or[A,B] = {
    type not[X]   = X => Nothing
    type and[X,Y] = X with Y
    type or[X,Y]  = not[not[X] and not[Y]]
    type F[X]     = not[not[X]] <:< (A or B)
  }

}
