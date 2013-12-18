package com.metamx.common.scala

import com.metamx.common.scala.untyped._

/**
 * Works with "untyped" to make it easier to extract specific things from nested, untyped structures.
 *
 * Somewhat experimental API; if this proves useful, we'll keep it.
 */
object chaincast
{
  implicit def chainCast[A](o: A) = new StartChainable(o)

  class StartChainable[A](o: A)
  {
    def chainCast = new Chainable(o)
  }

  class Chainable(o: Any)
  {
    def apply(s: String): Chainable = new Chainable(dict(o).apply(s))

    def get(s: String): Option[Chainable] = dict(o).get(s).map(new Chainable(_))

    def getOrElse[B](s: String, default: => B): Chainable = new Chainable(get(s).getOrElse(default))

    def asList: Seq[Chainable] = list(o).map(new Chainable(_))

    def asDict: Map[String, Chainable] = dict(o).map(kv => (kv._1, new Chainable(kv._2)))

    def asString = str(o)

    def asInt = int(o)

    def asLong = long(o)

    def asBool = bool(o)

    def asDouble = double(o)

    def asFloat = float(o)

    def unwrap = o
  }

}
