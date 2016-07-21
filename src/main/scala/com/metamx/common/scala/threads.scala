package com.metamx.common.scala

import com.metamx.common.scala.concurrent._
import org.scala_tools.time.TypeImports._
import com.metamx.common.scala.Predef._
import scala.util.control.NonFatal

object threads extends Logging
{
  class RunnerThread(name: String, quietPeriod: Option[Period], f: => Any) extends Thread with Logging
  {
    setName(name)
    setDaemon(true)

    @volatile private var terminated = false

    override def run(): Unit = {
      try {
        val quietMillis = quietPeriod.map(_.toStandardDuration.getMillis)

        while (!terminated && !isInterrupted) {
          try {
            val startMillis = System.currentTimeMillis()
            try {
              f
            } catch {
              case NonFatal(e) => log.error(e, "Exception while running thread [%s]".format(name))
            }

            quietMillis match {
              case Some(m) =>
                val waitMillis = startMillis + m - System.currentTimeMillis()
                if (waitMillis > 0) {
                  Thread.sleep(waitMillis)
                }

              case None => // Don't need to sleep
            }
          } catch {
            case e: InterruptedException =>
              if (terminated) {
                log.info("Thread [%s] terminated")
              } else {
                log.info("Thread [%s] interrupted")
              }
              interrupt()
          }
        }
      } catch {
        case e: Throwable => log.error(e, "Thread [%s] killed by exception".format(name))
      }
    }

    def terminate() {
      terminated = true
      interrupt()
    }
  }

  def startHaltingThread(body: => Any, name: String) = daemonThread { abortingRunnable {
    try body catch {
      case e: Throwable =>
        log.error(e, "Halting")
        Runtime.getRuntime.halt(1)
    }
  }} withEffect {
    t =>
      t.setName(name)
      t.start()
  }

  def runnerThread(name: String, f: => Any): RunnerThread = {
    runnerThread(name, None, f)
  }

  def runnerThread(name: String, quietPeriod: Period, f: => Any): RunnerThread = {
    runnerThread(name, Some(quietPeriod), f)
  }

  def initRunnerThread(name: String, f: => Any): RunnerThread = {
    new RunnerThread(name, None, f)
  }

  def initRunnerThread(name: String, quietPeriod: Period, f: => Any): RunnerThread = {
    new RunnerThread(name, Some(quietPeriod), f)
  }

  private def runnerThread(name: String, quietPeriod: Option[Period], f: => Any): RunnerThread = {
    val thread = new RunnerThread(name, quietPeriod, f)
    thread.start()
    thread
  }
}
