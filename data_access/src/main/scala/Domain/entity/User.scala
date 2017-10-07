package Domain.entity

import java.util.UUID

import com.mohiva.play.silhouette.api.Identity
import shapeless.tag.@@

sealed trait UserId

/**
  *
  *
  *
  * @param id
  * @param username
  * @param email
  * @param firstname
  * @param lastname
  */
final case class User(id: UUID @@ UserId,
                      username: String,
                      email: String,
                      firstname: String,
                      lastname: String)
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
