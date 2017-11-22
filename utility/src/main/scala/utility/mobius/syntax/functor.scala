package utility.mobius.syntax

import monocle.Iso
import cats.Functor

private[syntax] trait IsoFunctorSyntax {

  implicit class IsoFunctor[A, F[_]: Functor](underlying: F[A]) {
    def mapIsoGet[B](iso: Iso[A, B]): F[B] = Functor[F].map(underlying)(iso.get)

  }

  implicit class IsoReverseFunctor[B, F[_]: Functor](underlying: F[B]) {
    def mapIsoReverseGet[A](iso: Iso[A, B]): F[A] = Functor[F].map(underlying)(iso.reverseGet)
  }
}

trait ExtFunctorSyntax extends IsoFunctorSyntax
