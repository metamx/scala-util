package com.metamx.common.scala.net.curator

import com.metamx.common.scala.Logging
import org.apache.curator.framework.CuratorFramework
import org.apache.zookeeper.CreateMode
import org.apache.zookeeper.KeeperException.NodeExistsException

object CuratorUtils extends Logging
{
  /**
   * Creates or updates data in the given path. This operation is not atomic, so you need proper synchronization
   * if multiple clients are going to modify the same path.
   */
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

  /**
   * Recursively creates path if it doesn't exist. This operation is atomic if all clients trying to
   * create the same path use identical mode, otherwise path might be created with different mode
   * than asked by client.
   */
  private def createRecursiveIfNotExists(
    curator: CuratorFramework,
    path: String,
    createMode: Option[CreateMode] = None
  ) {
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
