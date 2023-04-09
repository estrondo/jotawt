package one.estrondo.jotawt

import scala.concurrent.duration.FiniteDuration

case class KeyProvider(
    name: String,
    delay: FiniteDuration,
    source: JWKSSource
)
