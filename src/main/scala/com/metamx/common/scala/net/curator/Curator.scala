package com.metamx.common.scala.net.curator

import com.metamx.common.lifecycle.Lifecycle
import com.metamx.common.lifecycle.Lifecycle.Handler
import org.joda.time.Duration
import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import org.apache.curator.retry.ExponentialBackoffRetry

object Curator
{
  def create(zkConnect: String, zkTimeout: Duration, lifecycle: Lifecycle): CuratorFramework = {
    val curator = CuratorFrameworkFactory
      .builder()
      .connectString(zkConnect)
      .sessionTimeoutMs(zkTimeout.getMillis.toInt)
      .retryPolicy(new ExponentialBackoffRetry(1000, 30))
      .build()

    lifecycle.addHandler(
      new Handler
      {
        def start() {
          curator.start()
        }

        def stop() {
          curator.close()
        }
      }
    )

    curator
  }

  def create(config: CuratorConfig, lifecycle: Lifecycle): CuratorFramework = {
    create(config.zkConnect, config.zkTimeout, lifecycle)
  }
}

trait CuratorConfig
{
  def zkConnect: String

  def zkTimeout: Duration
}
