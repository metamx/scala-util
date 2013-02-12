package com.metamx.common.scala

import scala.collection.mutable.ListBuffer
import scala.sys.process.{Process, ProcessLogger}

object process extends Logging {

  def backtick(cmd: String*): Seq[String] = {
    log.trace("Running: %s" format (cmd mkString " "))
    val buf = ListBuffer[String]()
    val status = Process(cmd) !
      ProcessLogger(out => buf += "%s" format out, err => log.warn("%s: %s", cmd mkString " ", err))
    if (status != 0) {
      throw new ProcessFailureException("Command failed: %s (status = %d)" format (cmd mkString " ", status))
    } else {
      buf.toSeq
    }
  }

  def system(cmd: String*) {
    log.trace("Running: %s" format (cmd mkString " "))
    val status = Process(cmd) ! ProcessLogger(line => log.warn("%s: %s", cmd mkString " ", line))
    if (status != 0) {
      throw new ProcessFailureException("Command failed: %s (status = %d)" format (cmd mkString " ", status))
    }
  }

  class ProcessFailureException(msg: String) extends Exception(msg)

}
