package com.metamx.common.scala.event

import com.metamx.common.scala.Logging
import com.metamx.common.scala.untyped.Dict
import Emitting._
import com.metamx.common.scala.LateVal.LateVal
import com.metamx.emitter.EmittingLogger

// TODO Implicits or cake to convert these runtime exceptions into compile-time errors?

@deprecated("Setting Emitting.emitter is annoying.", "Sometime")
trait Emitting extends Logging
{
  val WARN = com.metamx.common.scala.event.WARN
  val ERROR = com.metamx.common.scala.event.ERROR

  Emitting.requireEmitter()

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

  private[this] val _emitter = new LateVal[ServiceEmitter]

  // Same as `emitter`, but emphasizes that an exception will be thrown if the emitter is not found
  @deprecated("Setting Emitting.emitter is annoying.", "Sometime")
  def requireEmitter(): ServiceEmitter = _emitter.derefOption getOrElse {
    throw new IllegalStateException("Emitter not set! (Try Emitting.emitter = ...)")
  }

  // Same as `requireEmitter()`, but less verbose and emphasizes the return value over the exception behavior
  @deprecated("Setting Emitting.emitter is annoying.", "Sometime")
  def emitter: ServiceEmitter = requireEmitter()

  @deprecated("Setting Emitting.emitter is annoying.", "Sometime")
  def emitter_=(_emitter: ServiceEmitter) {
    this._emitter.assignIfEmpty(_emitter)
    EmittingLogger.registerEmitter(_emitter)
  }
}
