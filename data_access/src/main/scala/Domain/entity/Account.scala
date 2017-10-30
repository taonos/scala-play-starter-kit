package Domain.entity

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import eu.timepit.refined.collection._
import eu.timepit.refined.api.Refined
import utility.RefinedTypes.{EmailString, UsernameString}

/**
  *
  *
  * @param id
  * @param username
  * @param email
  */
final case class Account(id: UUID, username: UsernameString, email: EmailString) extends Identity

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
