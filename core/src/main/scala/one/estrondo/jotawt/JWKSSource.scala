package one.estrondo.jotawt

sealed trait JWKSSource

case class OpenIDConfigurationEndpoint(uri: String) extends JWKSSource

case class JWKSEndpoint(uri: String) extends JWKSSource
