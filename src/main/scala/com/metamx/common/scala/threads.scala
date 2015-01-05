package com.metamx.common.scala

import com.metamx.common.scala.concurrent._
import org.scala_tools.time.TypeImports._

object threads extends Logging
{
  def runnerThread(name: String, f: => Any): Thread = {
    runnerThread(name, None, f)
  }

  def runnerThread(name: String, quietPeriod: Period, f: => Any): Thread = {
    runnerThread(name, Some(quietPeriod), f)
  }

  def initRunnerThread(name: String, f: => Any): Thread = {
    initRunnerThread(name, None, f)
  }

  def initRunnerThread(name: String, quietPeriod: Period, f: => Any): Thread = {
    initRunnerThread(name, Some(quietPeriod), f)
  }

  private def runnerThread(name: String, quietPeriod: Option[Period], f: => Any): Thread = {
    val thread = initRunnerThread(name, quietPeriod, f)
    thread.start()
    thread
  }

  private def initRunnerThread(name: String, quietPeriod: Option[Period], f: => Any): Thread = {
    val runnable = loggingRunnable {
      val quietMillis = quietPeriod.map(_.toStandardDuration.getMillis)

      while (!Thread.currentThread().isInterrupted) {
        val startMillis = System.currentTimeMillis()
        try {
          f
        } catch {
          case e: Exception => log.error(e, "Exception while running %s".format(name))
        }

        quietMillis match {
          case Some(m) =>
            val waitMillis = startMillis + m - System.currentTimeMillis()
            if (waitMillis > 0) {
              Thread.sleep(waitMillis)
            }

          case None => // Don't need to sleep
        }

      }
    }

    val thread = new Thread(runnable)
    thread.setName(name)
    thread.setDaemon(true)

    thread
  }
}
