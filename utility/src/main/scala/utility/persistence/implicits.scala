package utility.persistence

import cats.Functor

object implicits {

  implicit class MappingFunctor[A, F[_]: Functor](underlying: F[A]) {
//    def toTable[B <: Table](f: A => B): F[B] = Functor[F].map(underlying)(f)
//
//    def toEntity[B <: Entity](f: A => B): F[B] = Functor[F].map(underlying)(f)

    def mappingTo[B](implicit f: A => B): F[B] = Functor[F].map(underlying)(f)
  }
}
