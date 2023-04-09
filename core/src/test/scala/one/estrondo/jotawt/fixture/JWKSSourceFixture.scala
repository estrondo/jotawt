package one.estrondo.jotawt.fixture

import one.estrondo.jotawt.JWKSSource
import one.estrondo.jotawt.OpenIDConfigurationEndpoint
import one.estrondo.jotawt.randomCode
import one.estrondo.jotawt.JWKSEndpoint

object JWKSSourceFixture:
  def createOpenIDConfiguration() =
    OpenIDConfigurationEndpoint(s"http://${randomCode()}/.well-known/openid-configuration")

  def createJWKSUri() =
    JWKSEndpoint(s"http://${randomCode()}/oauth/jwks")
