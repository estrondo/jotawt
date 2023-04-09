package one.estrondo.jotawt

import one.estrondo.jotawt.{JWKSSource, OpenIDConfigurationEndpoint, JWKSEndpoint}
trait JWKSFetcher:

  def get[F[_]: Effect](source: JWKSSource): F[JotawtJWKSet]

object JWKSFetcher:

  def apply(httpClient: HttpClient, jsonReader: JsonReader): JWKSFetcher =
    Impl(httpClient, jsonReader)

  private class Impl(httpClient: HttpClient, jsonReader: JsonReader) extends JWKSFetcher:

    override def get[F[_]: Effect](source: JWKSSource): F[JotawtJWKSet] =
      source match
        case OpenIDConfigurationEndpoint(uri) => loadFromOpenIDConfigurationEndpoint(uri)
        case JWKSEndpoint(uri)                => loadFromJWKSEndpoint(uri)

    private def loadFromJWKSEndpoint[F[_]: Effect](uri: String): F[JotawtJWKSet] =
      for
        content <- httpClient.get(uri)
        jwkSet  <- jsonReader.readJWKSet(content)
      yield jwkSet

    private def loadFromOpenIDConfigurationEndpoint[F[_]: Effect](uri: String): F[JotawtJWKSet] =
      for
        content  <- httpClient.get(uri)
        metadata <- jsonReader.readOpenIDProviderMetada(content)
        jwkSet   <- loadFromJWKSEndpoint(metadata.jkws_uri)
      yield jwkSet
