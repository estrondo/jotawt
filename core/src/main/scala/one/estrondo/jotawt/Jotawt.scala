package one.estrondo.jotawt

import java.security.Key
import java.util.Base64
import java.nio.charset.StandardCharsets

trait Jotawt[Token, Json, F[_]]:

  def decode(token: String): F[Token]

  def decodeJson(token: String): F[Json]

object Jotawt:

  private val JWTRegex = """^([^.]+)\.([^.]+)\.([^.]+)$""".r

  def apply[Token, Json, F[_]: Effect](
      framework: Framework[Token, Json],
      keyRegistry: KeyRegistry,
      jsonReader: JsonReader
  ): Jotawt[Token, Json, F] =
    new Impl(framework, keyRegistry, jsonReader)

  private class Impl[Token, Json, F[_]: Effect](
      framework: Framework[Token, Json],
      keyRegistry: KeyRegistry,
      jsonReader: JsonReader
  ) extends Jotawt[Token, Json, F]:

    override def decode(token: String): F[Token] =
      mapTokenError {
        for
          key <- searchKey(token)
          jwt <- framework.decode(token, key)
        yield jwt
      }

    override def decodeJson(token: String): F[Json] =
      mapTokenError {
        for
          key  <- searchKey(token)
          json <- framework.decodeJson(token, key)
        yield json
      }

    private def decodeBase64(encoded: String)(using decoder: Base64.Decoder): String =
      new String(decoder.decode(encoded), StandardCharsets.UTF_8)

    private def break(token: String): F[(String, String, String)] =
      Effect[F].attempt {
        given Base64.Decoder = Base64.getUrlDecoder()

        token match
          case JWTRegex(header, claim, signature) =>
            (decodeBase64(header), decodeBase64(claim), signature)
          case _                                  =>
            throw JotawtException.InvalidFormat("It was expected a Token with header.claim.signature format!")
      }

    private def mapTokenError[T](effect: F[T]): F[T] =
      effect.mapError(JotawtException.Token("An error happend on decoding the token!", _))

    private def searchKey(token: String): F[Key] =
      for
        components       <- break(token)
        (rawHeader, _, _) = components

        header <- jsonReader
                    .readTokenHeader(rawHeader)
                    .mapError(JotawtException.Header("An error happend on header reading!", _))
        key    <- searchKey(header)
      yield key

    private def searchKey(header: JotawtHeader): F[Key] =
      header.kid match
        case Some(kid) =>
          keyRegistry
            .search(kid)
            .mapError(JotawtException.Key("An error happend on kid searching!", _))

        case None =>
          keyRegistry.defaultKey
            .mapError(JotawtException.Key("An error happend when default key getting!", _))
