package one.estrondo.jotawt

import one.estrondo.jotawt.KeyReader

import java.security.Key
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import scala.collection.concurrent.TrieMap
import scala.collection.immutable.HashMap
import scala.concurrent.duration.FiniteDuration

trait KeyRegistry:

  def defaultKey[F[_]: Effect]: F[Key]

  def search[F[_]: Effect](kid: String): F[Key]

  def use[F[_]: Effect](
      executor: ScheduledExecutorService,
      fetcher: JWKSFetcher,
      factory: KeyReader,
      runners: KeyProviderRunner*
  ): F[KeyRegistry]

object KeyRegistry:

  def apply[F[_]: Effect](defaultKey: Option[Key] = None): KeyRegistry = new Impl(defaultKey)

  private class Record(val kid: String, val provider: KeyProvider, val key: Key)

  private class Impl(key: Option[Key]) extends KeyRegistry:

    private var cache = TrieMap.empty[String, Record]

    override def defaultKey[F[_]: Effect]: F[Key] =
      key match
        case Some(value) => Effect[F].attempt(value)
        case None        => Effect[F].failed(JotawtException.Key("There is no default key!"))

    override def search[F[_]: Effect](kid: String): F[Key] =
      cache.get(kid) match
        case Some(record) => Effect[F].attempt(record.key)
        case None         => Effect[F].failed(JotawtException.Key("Not found!"))

    override def use[F[_]: Effect](
        executor: ScheduledExecutorService,
        fetcher: JWKSFetcher,
        factory: KeyReader,
        runners: KeyProviderRunner*
    ): F[KeyRegistry] =
      for _ <- Effect[F].foreach(runners)(schedule(executor, fetcher, factory))
      yield this

    private def schedule[F[_]: Effect](executor: ScheduledExecutorService, fetcher: JWKSFetcher, factory: KeyReader)(
        runner: KeyProviderRunner
    ): F[KeyProviderRunner] =
      runner.schedule(executor, fetcher, factory, updateCache)

    private[jotawt] def updateCache[F[_]: Effect](provider: KeyProvider, newCache: Map[String, Key]): F[Unit] =
      Effect[F].attempt {
        val shouldUpdateOrRemove = cache
          .filter(_._2.provider == provider)
          .to(HashMap)

        val (shouldUpdate, shouldRemove) = shouldUpdateOrRemove.foldLeft((Vector.empty[Record], Vector.empty[String])) {
          case ((update, remove), (kid, record)) =>
            newCache.get(kid) match
              case Some(newKey) => (update :+ new Record(kid, provider, newKey), remove)
              case None         => (update, remove :+ kid)
        }

        for kid    <- shouldRemove do cache.remove(kid)
        for record <- shouldUpdate do cache(record.kid) = record
      }
