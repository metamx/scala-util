package com.metamx.common.scala.net.curator

import com.metamx.common.lifecycle.Lifecycle
import com.metamx.common.lifecycle.Lifecycle.Handler
import org.joda.time.Duration
import org.apache.curator.framework.{CuratorFrameworkFactory, CuratorFramework}
import org.apache.curator.retry.ExponentialBackoffRetry

object Curator
{
  def create(config: CuratorConfig, lifecycle: Lifecycle): CuratorFramework = {
    val curator = CuratorFrameworkFactory
      .builder()
      .connectString(config.zkConnect)
      .sessionTimeoutMs(config.zkTimeout.getMillis.toInt)
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
}

trait CuratorConfig
{
  def zkConnect: String

  def zkTimeout: Duration
}
