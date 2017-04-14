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

import scala.reflect.{ClassTag, classTag}
import scala.util.control.Exception.{catching => _catching}

// TODO Tests

object exception {

  /**
   * Returns a lazily-computed stream of causes for a particular Throwable. The provided Throwable will be returned
   * first, followed by its cause chain, if any.
   */
  def causes(e: Throwable): Stream[Throwable] = Stream.cons(e, Option(e.getCause) map causes getOrElse Stream.empty)

  /**
   * Checks if a Throwable, or any Throwable in its cause chain, matches a partial predicate.
   */
  def causeMatches(e: Throwable)(f: PartialFunction[Throwable, Boolean]) = causes(e).collect(f).contains(true)

  /**
   * Checks if a Throwable, or any Throwable in its cause chain, is a particular type.
   */
  def causedBy[E <: Throwable: ClassTag](e: Throwable) = causeMatches(e) {
    case x if classTag[E].runtimeClass.isAssignableFrom(x.getClass) => true
  }

  def raises[E <: Throwable] = new {
    def apply[X](x: => X)(implicit ct: ClassTag[E], ev: NotNothing[E]): Boolean =
      x.catchOption[E].isEmpty
  }

  def toOption[E <: Throwable] = new {
    def apply[X](x: => X)(implicit ct: ClassTag[E], ev: NotNothing[E]): Option[X] =
      x.catchOption[E]
  }

  def toEither[E <: Throwable] = new {
    def apply[X](x: => X)(implicit ct: ClassTag[E], ev: NotNothing[E]): Either[E,X] =
      x.catchEither[E]
  }

  class ExceptionOps[X](x: => X) {

    def mapException(f: PartialFunction[Throwable, Throwable]): X =
      try x catch {
        case e if f isDefinedAt e => throw f(e)
      }

    def catching[Y](f: PartialFunction[Throwable, Y]) = new {
      def orElse(y: => Y)   : Y = orElse(_ => y)
      def orElse(g: X => Y) : Y = {
        (try Left(x) catch {
          case e if f isDefinedAt e => Right(f(e))
        }) match {
          case Left(x)  => g(x)
          case Right(y) => y
        }
      }
    }

    def swallow(f: PartialFunction[Throwable, Any]): Option[X] =
      try Some(x) catch {
        case e if f.isDefinedAt(e) => f(e); None
      }

    def catchOption[E <: Throwable : ClassTag : NotNothing]: Option[X] =
      _catching(classTag[E].runtimeClass) opt x

    def catchEither[E <: Throwable : ClassTag : NotNothing]: Either[E,X] =
      (_catching(classTag[E].runtimeClass) either x).left map (_.asInstanceOf[E])

  }

  implicit def ExceptionOps[X](x: => X): ExceptionOps[X] = new ExceptionOps(x)
    // Bug: by-name implicit conversions don't work for X = Nothing
    // Workaround: manually upcast at the call site

  // Negative type bounds via ambiguous implicits [https://gist.github.com/206a147b7291fd5b2193]
  trait NotNothing[X]
  implicit def notNothing[X]            : NotNothing[X] = null // Accept NotNothing[X] for all X
  implicit def isNothing0[X <: Nothing] : NotNothing[X] = null // Reject NotNothing[Nothing]
  implicit def isNothing1[X <: Nothing] : NotNothing[X] = null

}
