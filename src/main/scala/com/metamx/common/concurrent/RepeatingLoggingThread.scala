package com.metamx.common.concurrent

import com.metamx.common.scala.Logging
import org.joda.time.Duration

class RepeatingLoggingThread(delay: Duration, name: String, body: => Any) extends Logging
{
  val thread = new RepeatingThread(delay, new Runnable {
    def run() {
      try {
        body
      } catch {
        case e: InterruptedException =>
          throw e

        case e: Exception =>
          log.error(e, "[%s] - Exception while performing operation".format(name))
      }
    }
  })
  thread.setName(name)

  def start() = thread.start()

  def cancel() = thread.cancel()

  def repeatNow() = thread.repeatNow()
}

