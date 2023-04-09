package one.estrondo.jotawt

import org.specs2.execute.Failure
import org.specs2.execute.Result
import org.specs2.execute.Success
import org.specs2.matcher.Expectable
import org.specs2.matcher.ExpectationsCreation
import org.specs2.matcher.Matcher

import scala.reflect.ClassTag

abstract class Spec[F[_]](using effectMatcher: EffectMatcher[F]) extends org.specs2.Specification, ExpectationsCreation:

  protected def fail[E: ClassTag](matcher: Matcher[E]): Matcher[F[Any]] =
    effectMatcher.fail[E](matcher)

  protected def success[T](matcher: Matcher[T]): Matcher[F[T]] =
    effectMatcher.success(matcher)

  protected def hasCause(matcher: Matcher[Throwable]): Matcher[Throwable] =
    new Matcher[Throwable]:
      override def apply[S <: Throwable](t: Expectable[S]): Result =

        def checkCause(cause: Throwable): Result =
          if cause == null then Failure("No cause!")
          else
            val result = matcher(theValue(cause))
            if result.isSuccess then result
            else checkCause(cause.getCause())

        checkCause(t.value.getCause())
