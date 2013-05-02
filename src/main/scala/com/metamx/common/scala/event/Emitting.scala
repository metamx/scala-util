package com.metamx.common.scala.event

import com.metamx.common.scala.Logging
import com.metamx.common.scala.untyped.Dict
import Emitting._
import com.metamx.common.scala.LateVal.LateVal
import com.metamx.emitter.EmittingLogger

trait Emitting extends Logging
{
  def emitter: ServiceEmitter = Emitting.emitter

  def emitAlert(e: Throwable, severity: Severity, description: String, data: Dict)
  {
    emit.emitAlert(e, log, emitter, severity, description, data)
  }

  def emitAlert(severity: Severity, description: String, data: Dict)
  {
    emit.emitAlert(null, log, emitter, severity, description, data)
  }
}

object Emitting
{
  type ServiceEmitter = com.metamx.emitter.service.ServiceEmitter
  type Severity = com.metamx.emitter.service.AlertEvent.Severity

  @volatile private[this] var _emitter: Option[ServiceEmitter] = None

  def emitter = _emitter getOrElse {
    throw new IllegalStateException("Emitter not set! (Try Emitting.emitter = ...)")
  }

  def emitter_=(_emitter: ServiceEmitter) {
    this._emitter = Some(_emitter)
    EmittingLogger.registerEmitter(_emitter)
  }
}
