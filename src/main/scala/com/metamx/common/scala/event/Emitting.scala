package com.metamx.common.scala.event

import com.metamx.common.scala.Logging
import com.metamx.common.scala.untyped.Dict
import com.metamx.common.scala.Logging._
import Emitting._

trait Emitting extends Logging
{
  implicit val emitter: ServiceEmitter = implicitly[ServiceEmitter]

  def emitAlert(e: Throwable, severity: Severity, description: String, data: Dict)
    (implicit log: Logger, emitter: ServiceEmitter)
  {
    emit.emitAlert(e, log, emitter, severity, description, data)
  }

  def emitAlert(severity: Severity, description: String, data: Dict)
    (implicit log: Logger, emitter: ServiceEmitter)
  {
    emit.emitAlert(null, log, emitter, severity, description, data)
  }
}

object Emitting
{
  type ServiceEmitter = com.metamx.emitter.service.ServiceEmitter
  type Severity = com.metamx.emitter.service.AlertEvent.Severity
}
