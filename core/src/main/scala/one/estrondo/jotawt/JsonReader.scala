package one.estrondo.jotawt

trait JsonReader:

  def readJWKSet[F[_]: Effect](raw: String): F[JotawtJWKSet]

  def readOpenIDProviderMetada[F[_]: Effect](raw: String): F[OpenIDProviderMetada]

  def readTokenHeader[F[_]: Effect](raw: String): F[JotawtHeader]
