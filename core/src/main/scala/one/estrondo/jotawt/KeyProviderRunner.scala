package one.estrondo.jotawt

import one.estrondo.jotawt.KeyReader

import java.security.Key
import java.util.ServiceLoader
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters.*

trait KeyProviderRunner:

  def schedule[F[_]: Effect](
      scheduledExecutorService: ScheduledExecutorService,
      fetcher: JWKSFetcher,
      keyFactory: KeyReader,
      callback: (KeyProvider, Map[String, Key]) => F[Unit]
  ): F[KeyProviderRunner]

object KeyProviderRunner:

  def apply(provider: KeyProvider): KeyProviderRunner =
    new Impl(provider)

  private class Impl(provider: KeyProvider) extends KeyProviderRunner:

    override def schedule[F[_]: Effect](
        executorService: ScheduledExecutorService,
        fetcher: JWKSFetcher,
        factory: KeyReader,
        callback: (KeyProvider, Map[String, Key]) => F[Unit]
    ): F[KeyProviderRunner] = Effect[F].attempt {

      executorService.scheduleAtFixedRate(
        Runner(fetcher, factory, callback),
        1000L,
        provider.delay.toMillis,
        TimeUnit.MILLISECONDS
      )

      this
    }

    private class Runner[F[_]: Effect](
        fetcher: JWKSFetcher,
        factory: KeyReader,
        callback: (KeyProvider, Map[String, Key]) => F[Unit]
    ) extends Runnable:

      override def run(): Unit = Effect[F].run {
        for
          jwkSet <- fetcher.get(provider.source)
          keys   <- Effect[F].foreach(jwkSet.keys)(factory.create)
          _      <- callback(provider, HashMap.from(keys))
        yield ()
      }
