package one.estrondo.jotawt.fixture

import one.estrondo.jotawt.JotawtJWK
import one.estrondo.jotawt.{randomCode, createRandomBigInteger, encodeBase64Url}

object JotawtJWKFixture:

  def createRSA() = JotawtJWK(
    kty = "RSA",
    kid = Some(randomCode(16)),
    use = Some("sig"),
    alg = Some("RS256"),
    n = Some(encodeBase64Url(createRandomBigInteger(64).toByteArray())),
    e = Some(encodeBase64Url(createRandomBigInteger(8).toByteArray()))
  )

  def createEC() = JotawtJWK(
    kty = "EC",
    kid = Some(randomCode(8)),
    use = Some("sig"),
    alg = Some("ES256"),
    crv = Some("P-256"),
    x = Some(encodeBase64Url(createRandomBigInteger(32).toByteArray())),
    y = Some(encodeBase64Url(createRandomBigInteger(32).toByteArray()))
  )
