package one.estrondo.jotawt

import scala.util.Try
import scala.util.Failure
import scala.util.Success

given Effect[[X] =>> Try[X]] with

  override def attempt[A](fn: => A): Try[A] = Try(fn)

  override def flatMap[A, B](a: Try[A], fn: A => Try[B]): Try[B] = a.flatMap(fn)

  override def map[A, B](a: Try[A], fn: A => B): Try[B] =
    a.map(fn)

  override def failed[A](cause: => Throwable): Try[A] = Failure(cause)

  override def run[A](a: Try[A]): A =
    a.get

  override def mapError[A](effect: Try[A], fn: Throwable => Throwable): Try[A] =
    effect match
      case Success(_) =>
        effect

      case Failure(cause) =>
        try Failure(fn(cause))
        catch case expected => Failure(cause)
