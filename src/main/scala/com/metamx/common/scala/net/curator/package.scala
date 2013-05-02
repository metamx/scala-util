package com.metamx.common.scala.net

import org.apache.curator.x.discovery.{ServiceProvider, ServiceInstance}

package object curator
{
  implicit def ServiceInstanceOps[T](x: ServiceInstance[T]) = new ServiceInstanceOps[T](x)
  implicit def ServiceProviderOps[T](x: ServiceProvider[T]) = new ServiceProviderOps[T](x)
}
