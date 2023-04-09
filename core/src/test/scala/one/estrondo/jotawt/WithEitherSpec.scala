package one.estrondo.jotawt

import _root_.one.estrondo.sweetmockito.{Effect => SweetEffect}

given SweetEffect[[X] =>> Either[Throwable, X], Throwable, Any] with

  override def failed[E1 <: Throwable, A1](error: => E1): Either[E1, A1] = Left(error)

  override def succeed[A1](value: => A1): Either[Throwable, A1] = Right(value)

given EffectMatcher[[X] =>> Either[Throwable, X]] with

  override def extractValue[A](a: Either[Throwable, A]): A =
    a match
      case Right(value) => value
      case Left(error)  => throw IllegalArgumentException("It was expected an right value!")

  override def extractError[A, E](value: Either[Throwable, A]): E =
    value match
      case Left(error)  => error.asInstanceOf[E]
      case Right(value) => throw IllegalStateException("It was expected a Left here!")

type TEither = [A] =>> Either[Throwable, A]

class JotawtWithEitherSpec extends AbstractJotawtSpec[TEither]

class KeyProviderWithEitherSpec extends AbstractKeyProviderRunner[TEither]

class JWKSFetcherWithEitherSpec extends AbstractJWKSFetcherSpec[TEither]
