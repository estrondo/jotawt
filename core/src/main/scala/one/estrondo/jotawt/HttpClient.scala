package one.estrondo.jotawt

trait HttpClient:

  def get[F[_]: Effect](uri: String): F[String]
