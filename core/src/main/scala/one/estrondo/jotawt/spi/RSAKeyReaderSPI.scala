package one.estrondo.jotawt.spi

import one.estrondo.jotawt.Effect
import one.estrondo.jotawt.JotawtException
import one.estrondo.jotawt.JotawtJWK
import one.estrondo.jotawt.KeyReader
import one.estrondo.jotawt.flatMap
import one.estrondo.jotawt.map

import java.math.BigInteger
import java.security.Key
import java.security.spec.RSAPublicKeySpec
import java.security.{KeyFactory => Underlying}
import scala.collection.immutable.HashSet

class RSAKeyReaderSPI extends KeyReaderSPI:

  override val kty: String = "RSA"

  override def supportedKty = HashSet(kty)

  private lazy val underlying = Underlying.getInstance("RSA")

  override def create[F[_]: Effect](jwk: JotawtJWK): F[(String, Key)] =
    (jwk.n, jwk.e, jwk.kid) match
      case (Some(n), Some(e), Some(kid)) =>
        for
          n <- decodeBase64Url(n)(BigInteger(_))
          e <- decodeBase64Url(e)(BigInteger(_))
        yield kid -> underlying.generatePublic(RSAPublicKeySpec(n, e))

      case _ =>
        Effect[F].failed(
          JotawtException.Key(
            "The following parameters are required in order to create a RSA Public Key: n, e and kid!"
          )
        )
