package com.metamx.common.scala.net.curator

import com.metamx.common.scala.Logging
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException

object CuratorUtils extends Logging
{
  def createOrUpdate(
    curator: CuratorFramework,
    path: String,
    create: => Array[Byte],
    update: (Array[Byte]) => Array[Byte]
  ) {
    if (curator.checkExists().forPath(path) == null) {
      curator.create().forPath(path, create)
    } else {
      val data = curator.getData.forPath(path)
      curator.setData().forPath(path, update(data))
    }
  }

  def createRecursiveIfNotExists(curator: CuratorFramework, path: String) {
    createRecursiveIfNotExists(curator, path, None)
  }

  def createRecursiveIfNotExists(curator: CuratorFramework, path: String, createMode: CreateMode) {
    createRecursiveIfNotExists(curator, path, Some(createMode))
  }

  private def createRecursiveIfNotExists(curator: CuratorFramework, path: String, createMode: Option[CreateMode]) {
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
