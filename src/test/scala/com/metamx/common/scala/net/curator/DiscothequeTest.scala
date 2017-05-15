package com.metamx.common.scala.net.curator

import com.metamx.common.ISE
import com.metamx.common.lifecycle.Lifecycle
import com.metamx.common.scala.Predef.EffectOps
import com.metamx.common.scala.control.ifException
import com.metamx.common.scala.control.retryOnErrors
import com.simple.simplespec.Matchers
import java.net.BindException
import org.apache.curator.test.TestingCluster
import org.hamcrest.CoreMatchers._
import org.junit.Test

class DiscothequeTest extends Matchers {

  @Test
  def testTheSameInstance(): Unit = {
    var lifecycle: Lifecycle = null
    var cluster: TestingCluster = null

    try {
      lifecycle = new Lifecycle().withEffect(_.start())
      cluster = newTestingZkCluster().withEffect(_.start())

      val discotheque = new Discotheque(lifecycle)

      val disco1 = discotheque.disco(cluster.getConnectString, "/test/discoPath")
      val disco2 = discotheque.disco(cluster.getConnectString, "/test/discoPath")

      disco1 must be(sameInstance(disco2))
    } finally {
      Option(lifecycle).foreach(_.stop())
      Option(cluster).foreach(_.stop())
    }
  }

  @Test
  def testAnnounce(): Unit = {
    var lifecycle: Lifecycle = null
    var cluster: TestingCluster = null

    try {
      lifecycle = new Lifecycle().withEffect(_.start())
      cluster = newTestingZkCluster().withEffect(_.start())

      val discotheque = new Discotheque(lifecycle)

      val announceConfig1 = DiscoAnnounceConfig("test:instance", 8080, false)

      val disco1 = discotheque.disco(cluster.getConnectString, "/test/discoPath", Some(announceConfig1))
      val disco2 = discotheque.disco(cluster.getConnectString, "/test/discoPath")

      disco1 must be(sameInstance(disco2))

      evaluating {
        discotheque.disco(cluster.getConnectString, "/test/discoPath", Some(announceConfig1.copy(port = 8081)))
      } must throwAn[ISE]
    } finally {
      Option(lifecycle).foreach(_.stop())
      Option(cluster).foreach(_.stop())
    }
  }

  private def newTestingZkCluster(size: Int = 1): TestingCluster = {
    retryOnErrors(ifException[BindException]) { new TestingCluster(size) }
  }
}
