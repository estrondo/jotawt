package one.estrondo.jotawt

import one.estrondo.jotawt.fixture.JotawtJWKFixture
import one.estrondo.jotawt.spi.RSAKeyReaderSPI

import java.security.interfaces.RSAPublicKey
import scala.util.Try

class RSAKeyFactorySpec extends Spec[Try]:

  override def is = s2"""
A RSAKeyFactory

  It should read a valid RSA JWK. ${shouldReadJWK}
  It should have RSA as kty value. ${shouldHavaRSAKty}
  It should reject a JWK with no kid. ${shouldRejectWithNoKty}
"""

  def shouldReadJWK =
    val jwk = JotawtJWKFixture.createRSA()

    val kid = jwk.kid.get
    val n   = decodeBase64urlAsBigInteger(jwk.n.get)
    val e   = decodeBase64urlAsBigInteger(jwk.e.get)

    RSAKeyReaderSPI().create[Try](jwk) must beSuccessfulTry.like { case (kid, key: RSAPublicKey) =>
      (key.getModulus(), key.getPublicExponent()) must beEqualTo((n, e))
    }

  def shouldHavaRSAKty =
    new RSAKeyReaderSPI().kty must beEqualTo("RSA")

  def shouldRejectWithNoKty =
    val jwk = JotawtJWKFixture.createRSA().copy(kid = None)

    RSAKeyReaderSPI().create[Try](jwk) must beFailedTry
