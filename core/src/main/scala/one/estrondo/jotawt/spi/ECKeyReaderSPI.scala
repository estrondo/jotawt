package one.estrondo.jotawt.spi

import one.estrondo.jotawt.Effect
import one.estrondo.jotawt.JotawtException
import one.estrondo.jotawt.JotawtJWK
import one.estrondo.jotawt.KeyReader
import one.estrondo.jotawt.flatMap
import one.estrondo.jotawt.map

import java.math.BigInteger
import java.security.AlgorithmParameters
import java.security.Key
import java.security.spec.ECGenParameterSpec
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.ECPublicKeySpec
import java.security.{KeyFactory => Underlying}
import scala.collection.immutable.HashMap
import scala.collection.immutable.HashSet

class ECKeyReaderSPI extends KeyReaderSPI:

  override val kty: String = "EC"

  override def supportedKty = HashSet(kty)

  private lazy val underlying = Underlying.getInstance("EC", "SunEC")

  private val SupportedCrvNames = HashMap(
    "P-256" -> "secp256r1",
    "P-384" -> "secp384r1",
    "P-521" -> "secp521r1"
  )

  override def create[F[_]: Effect](jwk: JotawtJWK): F[(String, Key)] =
    (jwk.kid, jwk.crv, jwk.x, jwk.y) match
      case (Some(kid), Some(crv), Some(x), Some(y)) =>
        for
          curveParamSpec <- getCurveSpec(crv)
          x              <- decodeBase64Url(x)(BigInteger(_))
          y              <- decodeBase64Url(y)(BigInteger(_))
        yield kid -> underlying.generatePublic(ECPublicKeySpec(ECPoint(x, y), curveParamSpec))

      case _ =>
        Effect[F].failed(JotawtException.Key("The following paramters are required: kid, crv, x and y!"))

  private def getCurveSpec[F[_]: Effect](name: String): F[ECParameterSpec] =
    SupportedCrvNames.get(name) match
      case Some(curveName) =>
        for parameters <- Effect[F].attempt(AlgorithmParameters.getInstance("EC", "SunEC"))
        yield
          parameters.init(ECGenParameterSpec(curveName))
          parameters.getParameterSpec(classOf[ECParameterSpec])

      case None =>
        Effect[F].failed(JotawtException.Key("Invalid crv value!"))
