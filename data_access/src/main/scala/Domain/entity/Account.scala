package Domain.entity

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import eu.timepit.refined.api.RefType
import utility.RefinedTypes.{EmailString, UsernameString}

/**
  * This ADT represents the different login providers.
  */
sealed trait LoginProvider {

  import LoginProvider._

  def isAuthenticatedViaCredentials: Boolean = this match {
    case Credentials(_) => true
    case _              => false
  }
}

object LoginProvider {

  final case class Credentials(key: EmailString) extends LoginProvider

  object Credentials {

    @throws[IllegalArgumentException]
    def unsafeFrom(key: String): Credentials =
      Credentials(RefType.applyRef[EmailString].unsafeFrom(key))
  }

  @throws[IllegalArgumentException]
  def unsafeFrom(provider: String, key: String): LoginProvider = provider match {
    case "credentials" => Credentials.unsafeFrom(key)
    // TODO: perhaps log the invalid input??
    case _ => throw new IllegalArgumentException("Invalid path")
  }
}

/**
  *
  *
  * @param id
  * @param username
  * @param email
  */
final case class Account(id: UUID,
                         username: UsernameString,
                         email: EmailString,
                         loginProvider: LoginProvider)
    extends Identity

/**
  *
  * Some implementation of the password hasher, for instance BCryptSha256PasswordHasher, embeds the salt with the
  * hashed password. In which case the salt would be empty.
  *
  * @param id
  * @param hasher The ID of the hasher used to hash this password.
  * @param password The hashed password.
  * @param salt The optional salt used when hashing.
  */
final case class UserPassword(id: UUID,
                              hasher: String,
                              password: String,
                              salt: Option[String] = None)
