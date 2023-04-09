package one.estrondo.jotawt

import scala.annotation.tailrec
import scala.collection.Factory

object Effect:
  transparent inline def apply[F[_]](using inline effect: Effect[F]): Effect[F] = effect

trait Effect[F[_]]:

  def attempt[A](fn: => A): F[A]

  def failed[A](cause: => Throwable): F[A]

  def flatMap[A, B](a: F[A], fn: A => F[B]): F[B]

  def foreach[A, B, C[E] <: Iterable[E]](iterable: C[A])(fn: A => F[B])(using
      factory: Factory[B, C[B]]
  ): F[C[B]] =
    val builder = factory.newBuilder

    def add(fb: F[B], tail: Iterable[A]): F[C[B]] =
      flatMap(
        fb,
        b => {
          builder.addOne(b)
          next(tail)
        }
      )

    def next(current: Iterable[A]): F[C[B]] =
      current.headOption match
        case Some(head) => add(flatMap(attempt(head), fn), current.tail)
        case None       => attempt(builder.result())

    next(iterable)

  def map[A, B](a: F[A], fn: A => B): F[B]

  def mapError[A](effect: F[A], fn: Throwable => Throwable): F[A]

  def run[A](effect: F[A]): A

extension [A, F[_]](effect: F[A])(using Effect[F])

  def map[B](fn: A => B): F[B] =
    Effect[F].map(effect, fn)

  def flatMap[B](fn: A => F[B]): F[B] =
    Effect[F].flatMap(effect, fn)

  def mapError(fn: Throwable => Throwable): F[A] =
    Effect[F].mapError(effect, fn)
