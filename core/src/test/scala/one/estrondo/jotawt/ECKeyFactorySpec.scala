package one.estrondo.jotawt

import one.estrondo.jotawt.fixture.JotawtJWKFixture
import one.estrondo.jotawt.spi.ECKeyReaderSPI
import org.specs2.specification.core.SpecStructure

import java.security.KeyFactory
import java.security.interfaces.ECPublicKey
import java.security.spec.ECPublicKeySpec
import scala.util.Try

class ECKeyFactorySpec extends Spec[Try]:

  override def is  = s2"""
An ECKeyFactory:
  It should read a valid EC JWK with curve = P-256. ${shouldReadJWK("P-256")}
  It should read a valid EC JWK with curve = P-384. ${shouldReadJWK("P-384")}
  It should read a valid EC JWK with curve = P-521. ${shouldReadJWK("P-521")}
  It should reject a JWK with no kid. ${rejectWithNoKid}
  It should reject a JWK with an unsupported EC curve. ${rejectInvalidCurve}
  It should have EC as kty value. ${ktyEqualToEC}
"""
  def ktyEqualToEC =
    ECKeyReaderSPI().kty === "EC"

  def shouldReadJWK(crv: String) =
    val jwk = JotawtJWKFixture
      .createEC()
      .copy(crv = Some(crv))

    val kid = jwk.kid.get

    val x = decodeBase64urlAsBigInteger(jwk.x.get)
    val y = decodeBase64urlAsBigInteger(jwk.y.get)

    ECKeyReaderSPI().create[Try](jwk) must beSuccessfulTry.like { case (kid, key: ECPublicKey) =>
      val spec = KeyFactory.getInstance("EC", "SunEC").getKeySpec(key, classOf[ECPublicKeySpec])
      (spec.getW().getAffineX(), spec.getW().getAffineY()) must beEqualTo(x, y)
    }

  def rejectWithNoKid =
    val jwk = JotawtJWKFixture.createEC().copy(kid = None)

    ECKeyReaderSPI().create[Try](jwk) must beFailedTry

  def rejectInvalidCurve =
    val jwk = JotawtJWKFixture
      .createEC()
      .copy(crv = Some("P-666"))

    val kid = jwk.kid.get

    val x = decodeBase64urlAsBigInteger(jwk.x.get)
    val y = decodeBase64urlAsBigInteger(jwk.y.get)

    ECKeyReaderSPI().create[Try](jwk) must beFailedTry
