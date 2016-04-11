package com.metamx.common.scala.net.curator

import com.metamx.common.lifecycle.Lifecycle.Handler
import com.metamx.common.lifecycle.{Lifecycle, LifecycleStart, LifecycleStop}
import java.net.URI
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.x.discovery.{ServiceInstance, _}
import scala.collection.JavaConverters._

abstract class AbstractDisco[T](curator: CuratorFramework, config: DiscoConfig, payload: Option[T] = None)(clazz: Class[T])
{
  val me: Option[ServiceInstance[T]] = config.discoAnnounce map {
    service =>
      val builder = ServiceInstance.builder[T]().name(service.name).payload(payload.orNull(null))
      if (service.ssl) {
        builder.sslPort(service.port)
      } else {
        builder.port(service.port)
      }

      builder.build()
  }

  val disco: ServiceDiscovery[T] = {
    val builder = ServiceDiscoveryBuilder.builder(clazz)
      .basePath(config.discoPath)
      .client(curator)

    if (me.isDefined) {
      builder.thisInstance(me.get)
    }

    builder.build()
  }

  def providerFor(service: String, lifecycle: Lifecycle) = {
    val provider = disco.serviceProviderBuilder().serviceName(service).build()

    lifecycle.addHandler(
      new Handler
      {

        def start() {
          provider.start()
        }

        def stop() {
          provider.close()
        }
      }
    )

    provider
  }

  def cacheFor(service: String, lifecycle: Lifecycle) = {
    val cache = disco.serviceCacheBuilder().name(service).build()

    lifecycle.addHandler(
      new Handler
      {
        def start() {
          cache.start()
        }

        def stop() {
          cache.close()
        }
      }
    )

    cache
  }

  /**
   * Discovers a URI once, without a provider. This should be avoided in high volume use cases.
   */
  def instanceFor(service: String): Option[ServiceInstance[T]] = disco.queryForInstances(service).asScala.headOption

  @LifecycleStart
  def start() {
    disco.start()
  }

  @LifecycleStop
  def stop() {
    disco.close()
  }
}

class Disco(curator: CuratorFramework, config: DiscoConfig) extends AbstractDisco[Void](curator, config)(Void.TYPE)

/**
 * Please note, that if you use PayloadDisco with not null payload you will get a ClassCastException when you try
 * to get this data using the Disco class. But you can use PayloadDisco to get data which was written by Disco class.
 * If you want to update your service's announce from Disco to PayloadDisco, first make sure that all clients
 * of this service are updated to PayloadDisco.
 */
class PayloadDisco(
  curator: CuratorFramework, config: DiscoConfig, payload: Option[Array[Byte]] = None
) extends AbstractDisco[Array[Byte]](curator, config, payload)(classOf[Array[Byte]])
{
  def this(curator: CuratorFramework, config: DiscoConfig, payload: Array[Byte]) = {
    this(curator, config, Some(payload))
  }
}

class ServiceProviderOps[T](provider: ServiceProvider[T])
{
  def instance: Option[ServiceInstance[T]] = Option(provider.getInstance())
}

class ServiceCacheOps[T](cache: ServiceCache[T])
{
  def instances: Seq[ServiceInstance[T]] = cache.getInstances.asScala
}

class ServiceInstanceOps[T](service: ServiceInstance[T])
{
  def name = service.getName

  def id = service.getId

  def port = service.getPort

  def sslPort = service.getSslPort

  def payload = Option(service.getPayload)

  def registrationTimeUTC = service.getRegistrationTimeUTC

  def serviceType = service.getServiceType

  def uriSpec = service.getUriSpec

  /**
   * Extract a usable URI from this ServiceInstance. Will use the uriSpec if present, or otherwise will
   * attempt some reasonable default.
   */
  def uri = Option(uriSpec) map (uriSpec => new URI(uriSpec.build(service))) getOrElse {
    val (proto, port) = if (service.getSslPort != null && service.getSslPort > 0) {
      ("https", service.getSslPort)
    } else {
      ("http", service.getPort)
    }

    new URI(proto, "%s:%s" format(service.getAddress, port), "/", null, null)
  }
}

trait DiscoConfig
{
  def discoPath: String

  def discoAnnounce: Option[DiscoAnnounceConfig]
}

case class DiscoAnnounceConfig(name: String, port: Int, ssl: Boolean)
