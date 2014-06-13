package com.metamx.common.scala.net.finagle

import com.google.common.util.concurrent.ThreadFactoryBuilder
import com.metamx.common.scala.Logging
import com.twitter.finagle.util.{DefaultTimer, InetSocketAddressUtil}
import com.twitter.finagle.{Addr, Resolver}
import com.twitter.util.{Closable, Future, FuturePool, Timer, Var, Duration => TwitterDuration}
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Like the InetResolver in Finagle, but periodically re-resolves names. (The built-in InetResolver does not, at least
 * as of Finagle 6.16.0). Like the built-in resolver, initial resolution is synchronous. Re-resolution occurs in the
 * background.
 *
 * Names are resolved roughly every '''networkaddress.cache.ttl''' seconds.
 *
 * @param ttl How often to re-resolve names
 * @param futurePool FuturePool used for background name resolution
 * @param timer Timer used to schedule background name resolution
 */
class InetAddressResolver(ttl: TwitterDuration, futurePool: FuturePool, timer: Timer)
  extends Resolver with Logging
{
  override val scheme = "inetaddr"

  override def bind(arg: String) = Var.async[Addr](Addr.Pending) {
    updatable =>
      updatable.update(resolveString(arg))
      val again = new AtomicBoolean(true)
      def schedule() {
        timer.doLater(ttl) {
          futurePool {
            updatable.update(resolveString(arg))
          } ensure {
            if (again.get()) {
              schedule()
            }
          }
        }
      }
      schedule()
      Closable.make {
        deadline =>
          again.set(false)
          Future.Done
      }
  }

  private def resolveString(arg: String) = Addr.Bound(InetSocketAddressUtil.parseHosts(arg): _*)
}

object InetAddressResolver
{
  def default = DefaultInetAddressResolver
}

object DefaultInetAddressResolver extends InetAddressResolver(
  TwitterDuration.fromSeconds(60),
  FuturePool(
    Executors.newSingleThreadExecutor(
      new ThreadFactoryBuilder()
        .setNameFormat("InetAddressResolver-Default")
        .setDaemon(false)
        .build()
    )
  ),
  DefaultTimer.twitter
)
