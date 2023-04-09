package one.estrondo.jotawt

case class JotawtJWK(
    kty: String,
    kid: Option[String],
    use: Option[String],
    alg: Option[String],

    // RSA attributes
    n: Option[String] = None,
    e: Option[String] = None,

    // EC
    crv: Option[String] = None,
    y: Option[String] = None,
    x: Option[String] = None
)
