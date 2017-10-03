package DAL.table

import java.util.UUID

import DAL.DAO.{PK, TableWithPK}

final case class AccountId(value: UUID) extends PK

object AccountId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, AccountId](AccountId.apply)
}

final case class AccountUsername(value: String)

object AccountUsername {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountUsername, String](_.value)
  implicit val decode = MappedEncoding[String, AccountUsername](AccountUsername.apply)
}

/**
  *
  * @param id An UUID that uniquely identifies a user.
  * @param username An unique username.
  * @param email An unique email.
  * @param firstname First name.
  * @param lastname Last name.
  * @param hasher
  * @param password
  * @param salt
  * @param providerId The ID of the provider.
  * @param providerKey A unique key which identifies a user on this provider (userID, email, ...).
  */
final case class AccountTable(id: AccountId,
                              username: AccountUsername,
                              email: String,
                              firstname: String,
                              lastname: String,
                              hasher: String,
                              password: String,
                              salt: Option[String],
                              providerId: String,
                              providerKey: String)
    extends TableWithPK[AccountId]
    with Timestamped {

  val pk: AccountId = id
}
