package com.metamx.common.scala.net.curator

import com.metamx.common.ISE
import com.metamx.common.lifecycle.Lifecycle
import com.metamx.common.scala.collection.concurrent.PermanentMap
import org.apache.curator.framework.CuratorFramework
import org.joda.time.Duration

/**
  * Collection of Disco instances to avoid multiple instances creation for the same endpoint (zkConnect, discoPath).
  *
  * If DiscoAnnounceConfig is defined and disco for this endpoint already created for different announce config then
  * [[IllegalStateException]] will be thrown.
  *
  * All methods are thread safe.
 */
class Discotheque(lifecycle: Lifecycle)
{
  private val curators = new PermanentMap[CuratorConfig, CuratorFramework]()
  private val discos = new PermanentMap[DiscoEndpoint, (Disco, Option[DiscoAnnounceConfig])]()

  def disco(config: CuratorConfig with DiscoConfig): Disco = {
    disco(config.zkConnect, config.discoPath, config.discoAnnounce, config.zkTimeout)
  }

  def disco(curatorConfig: CuratorConfig, discoConfig: DiscoConfig): Disco = {
    disco(curatorConfig.zkConnect, discoConfig.discoPath, discoConfig.discoAnnounce, curatorConfig.zkTimeout)
  }

  def disco(
    discoZkConnect: String,
    discoPath: String,
    discoAnnounceConfig: Option[DiscoAnnounceConfig] = None,
    discoZkTimeout: Duration = Duration.parse("PT15S")
  ): Disco = {
    val endpoint = DiscoEndpoint(discoZkConnect, discoPath)

    val (disco, announceConfig) = discos.getOrElseUpdate(endpoint, {
      val curatorConfig = endpoint.toCuratorConfig(discoZkTimeout)
      val curator = curators.getOrElseUpdate(curatorConfig, Curator.create(curatorConfig, lifecycle))

      val discoConfig = endpoint.toDiscoConfig(discoAnnounceConfig)
      (lifecycle.addMaybeStartManagedInstance(new Disco(curator, discoConfig)), discoAnnounceConfig)
    })

    if (discoAnnounceConfig.isDefined && discoAnnounceConfig != announceConfig) {
      throw new ISE("Failed to create announce [%s] for zkConnect[%s] and path[%s]. Already announced to [%s]".format(
        discoAnnounceConfig.toString,
        discoZkConnect,
        discoPath,
        announceConfig.map(_.toString).getOrElse("None")
      ))
    }

    disco
  }

  private case class DiscoEndpoint(discoZkConnect: String, discoPath: String)
  {
    def toCuratorConfig(zookeeperTimeout: Duration): CuratorConfig = new CuratorConfig
    {
      override def zkTimeout: Duration = zookeeperTimeout

      override def zkConnect: String = DiscoEndpoint.this.discoZkConnect
    }

    def toDiscoConfig(discoAnnounceConfig: Option[DiscoAnnounceConfig]) = new DiscoConfig
    {
      def discoAnnounce = discoAnnounceConfig

      def discoPath = DiscoEndpoint.this.discoPath
    }
  }
}
