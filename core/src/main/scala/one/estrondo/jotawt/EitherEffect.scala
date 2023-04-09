package one.estrondo.jotawt

import scala.util.Try

given Effect[[X] =>> Either[Throwable, X]] with

  override def attempt[A](fn: => A): Either[Throwable, A] =
    Try(fn).toEither

  override def flatMap[A, B](a: Either[Throwable, A], fn: A => Either[Throwable, B]): Either[Throwable, B] =
    a.flatMap(fn)

  override def map[A, B](a: Either[Throwable, A], fn: A => B): Either[Throwable, B] =
    a.map(fn)

  override def failed[A](error: => Throwable): Either[Throwable, A] = Left(error)

  override def run[A](either: Either[Throwable, A]): A =
    either match
      case Right(value) => value
      case Left(error)  => throw error

  override def mapError[A](effect: Either[Throwable, A], fn: Throwable => Throwable): Either[Throwable, A] =
    effect match
      case Left(error) =>
        Left(fn(error))
      case _           =>
        effect
