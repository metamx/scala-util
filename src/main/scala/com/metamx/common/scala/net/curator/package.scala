package com.metamx.common.scala.net

import com.netflix.curator.x.discovery.ServiceInstance

package object curator
{
  implicit def enrichServiceInstance[T](x: ServiceInstance[T]) = new ServiceInstanceOps[T](x)
}
