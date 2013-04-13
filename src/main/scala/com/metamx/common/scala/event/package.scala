package com.metamx.common.scala

import com.metamx.emitter.service.AlertEvent.Severity._
import com.metamx.emitter.service._
import com.metamx.emitter.core.Event
import scala.collection.JavaConverters._

package object event
{
  val WARN  = ANOMALY
  val ERROR = COMPONENT_FAILURE

  class ServiceMetricEventOps(e: ServiceMetricEvent) extends ServiceEventOps(e)
  {
    def userDims = e.getUserDims.asScala
    def metric = e.getMetric
    def value = e.getValue
  }
  implicit def ServiceMetricEventOps(e: ServiceMetricEvent) = new ServiceMetricEventOps(e)

  class AlertEventOps(e: AlertEvent) extends ServiceEventOps(e)
  {
    def severity = e.getSeverity
    def description = e.getDescription
    def dataMap = e.getDataMap.asScala
  }
  implicit def AlertEventOps(e: AlertEvent) = new AlertEventOps(e)

  class ServiceEventOps(e: ServiceEvent) extends EventOps(e)
  {
    def host = e.getHost
    def service = e.getService
  }
  implicit def ServiceEventOps(e: ServiceEvent) = new ServiceEventOps(e)

  class EventOps(e: Event)
  {
    def feed = e.getFeed
    def createdTime = e.getCreatedTime
    def safeToBuffer = e.isSafeToBuffer
  }
  implicit def EventOps(e: Event) = new EventOps(e)
}
