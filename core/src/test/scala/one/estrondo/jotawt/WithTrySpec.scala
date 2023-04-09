package one.estrondo.jotawt

import scala.util.Try

given EffectMatcher[Try] with

  override def extractValue[A](a: Try[A]): A =
    a match
      case util.Success(value)     => value
      case util.Failure(exception) => throw IllegalArgumentException("It was expected a success!")

  override def extractError[A, E](value: Try[A]): E =
    value match
      case util.Failure(exception) => exception.asInstanceOf[E]
      case util.Success(value)     => throw IllegalStateException("Expected a failure!")

class JotawtWithTrySpec extends AbstractJotawtSpec[Try]

class KeyProviderRunnerWithTrySpec extends AbstractKeyProviderRunner[Try]

class JWKSFetcherWithTrySpec extends AbstractJWKSFetcherSpec[Try]
