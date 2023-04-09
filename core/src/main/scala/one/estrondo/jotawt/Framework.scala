package one.estrondo.jotawt

import java.security.Key

trait Framework[Token, Json]:

  def decode[F[_]: Effect](token: String, key: Key): F[Token]

  def decodeJson[F[_]: Effect](token: String, key: Key): F[Json]
