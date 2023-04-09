package one.estrondo.jotawt.fixture

import one.estrondo.jotawt.JWKSSource
import one.estrondo.jotawt.KeyProvider
import one.estrondo.jotawt.randomCode

import scala.concurrent.duration.DurationInt

object KeyProviderFixture:

  def create(source: JWKSSource): KeyProvider = KeyProvider(
    name = s"provider-${randomCode()}",
    delay = 30.minutes,
    source = source
  )
