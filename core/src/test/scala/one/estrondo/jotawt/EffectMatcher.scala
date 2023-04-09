package one.estrondo.jotawt

import scala.reflect.ClassTag
import org.specs2.matcher.Matcher
import org.specs2.matcher.ExpectationsCreation

trait EffectMatcher[F[_]] extends ExpectationsCreation:

  protected def extractError[A, E](a: F[A]): E

  protected def extractValue[A](a: F[A]): A

  def fail[E](matcher: Matcher[E]): Matcher[F[Any]] =
    matcher ^^ extractError[Any, E]

  def success[A](matcher: Matcher[A]): Matcher[F[A]] =
    matcher ^^ extractValue[A]
