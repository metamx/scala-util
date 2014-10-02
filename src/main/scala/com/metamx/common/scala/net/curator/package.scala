package com.metamx.common.scala.net

import com.metamx.common.scala.Logging
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.x.discovery.{ServiceProvider, ServiceInstance}
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException

package object curator
{
  implicit def ServiceInstanceOps[T](x: ServiceInstance[T]) = new ServiceInstanceOps[T](x)
  implicit def ServiceProviderOps[T](x: ServiceProvider[T]) = new ServiceProviderOps[T](x)

  class CuratorFrameworkOps(curator: CuratorFramework) extends Logging
  {
    def zkConnectionStr = curator.getZookeeperClient.getCurrentConnectionString

    def createOrUpdate(path: String, create: => Array[Byte], update: (Array[Byte]) => Array[Byte]) {
      if (curator.checkExists().forPath(path) == null) {
        curator.create().forPath(path, create)
      } else {
        val data = curator.getData.forPath(path)
        curator.setData().forPath(path, update(data))
      }
    }

    def createRecursiveIfNotExists(path: String) {
      createRecursiveIfNotExists(path, None)
    }

    def createRecursiveIfNotExists(path: String, createMode: CreateMode) {
      createRecursiveIfNotExists(path, Some(createMode))
    }

    private def createRecursiveIfNotExists(path: String, createMode: Option[CreateMode]) {
      try {
        if (curator.checkExists().forPath(path) == null) {
          val builder = curator.create().creatingParentsIfNeeded()
          val builderWithMode = createMode match {
            case Some(mode) => builder.withMode(mode)
            case None => builder
          }
          builderWithMode.forPath(path)
        }
      } catch {
        case e: NodeExistsException => log.info("Concurrent path creation: %s".format(path))
      }
    }
  }

  implicit def CuratorFrameworkOps(curator: CuratorFramework) = new CuratorFrameworkOps(curator)
}
