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

package object concurrent {

  import com.github.nscala_time.time.Imports._
  import com.metamx.common.scala.Predef._
  import com.metamx.common.scala.exception._
  import java.util.concurrent.{Callable, ExecutionException, Executors}
  import scala.util.Random

  // loggingRunnable and abortingRunnable are almost always better than these
  //implicit def asRunnable(f: () => Unit) = new Runnable { def run = f }
  //implicit def asCallable[X](f: () => X) = new Callable[X] { def call = f() }

  def loggingRunnable(body: => Any) = new Runnable with Logging {
    override def run {
      try body catch {
        case e: Throwable => log.error(e, "Killed by exception")
      }
    }
  }

  def abortingRunnable(body: => Any) = new Runnable {
    override def run {
      try body catch {
        case e: Throwable => Abort(e)
      }
    }
  }

  def loggingThread(body: => Any)  = daemonThread(loggingRunnable(body))
  def abortingThread(body: => Any) = daemonThread(abortingRunnable(body))
  def daemonThread(f: Runnable)    = new Thread(f) withEffect { _ setDaemon true }

  def callable[X](x: => X) = new Callable[X] { def call = x }

  def numCores = Runtime.getRuntime.availableProcessors
  def par[X,Y](xs: Iterable[X], threads: Int = numCores)(f: X => Y): Vector[Y] = {
    // FIXME Creating a new pool for each call is dangerous... Should every caller really maintain their own pool...?
    Executors.newFixedThreadPool(threads).withFinally(_.shutdown) { exec =>
      Vector() ++ xs map { x => exec.submit(callable { f(x) }) } map (_.get)
    } mapException {
      case e: ExecutionException => e.getCause // Wrapped exceptions are anti-useful; unwrap them
    }
  }

  // TODO Avoid drift
  def everyFuzzy(period: Duration, fuzz: Double, delay: Boolean = true)(f: => Unit) {
    if (delay) Thread.sleep((period.millis * Random.nextDouble).toLong) // Randomize phase
    forever {
      f
      Thread.sleep((period.millis * math.max(1 + fuzz * Random.nextGaussian, 0)).toLong) // Fuzz period
    }
  }

  def spawn(body: => Any) {
    loggingThread(body).start
  }

  def after(millis: Long)(f: => Unit) {
    spawn {
      Thread sleep millis
      f
    }
  }

}
