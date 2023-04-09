package one.estrondo.jotawt

import one.estrondo.jotawt.Effect
import one.estrondo.jotawt.JotawtException
import one.estrondo.jotawt.JotawtJWK
import one.estrondo.jotawt.spi.KeyReaderSPI

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Base64
import java.util.ServiceLoader
import scala.collection.immutable.HashSet
import scala.jdk.CollectionConverters.IteratorHasAsScala

trait KeyReader:

  def create[F[_]: Effect](jwk: JotawtJWK): F[(String, Key)]

  def supportedKty: Set[String]

  protected lazy val base64UrlDecoder = Base64.getUrlDecoder()

  protected def decodeBase64Url[T, F[_]: Effect](raw: String)(fn: Array[Byte] => T): F[T] =
    Effect[F].attempt {
      fn(base64UrlDecoder.decode(raw))
    }

object KeyReader extends KeyReader:

  override def supportedKty: Set[String] = HashSet.from(providers.map(_.kty))

  private def providers = ServiceLoader
    .load(classOf[KeyReaderSPI])
    .iterator()
    .asScala

  override def create[F[_]: Effect](jwk: JotawtJWK): F[(String, Key)] =
    providers
      .find(_.kty == jwk.kty) match
      case Some(provider) => provider.create(jwk)
      case None           => Effect[F].failed(JotawtException.Key(s"There is no support for kty: ${jwk.kty}!"))
