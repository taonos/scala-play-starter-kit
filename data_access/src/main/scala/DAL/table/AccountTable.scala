package DAL.table

import java.util.UUID

import DAL.DAO.{PK, TableWithPK}
import eu.timepit.refined.api.RefType
import utility.RefinedTypes.{EmailString, NonEmptyString, UsernameString}

final case class AccountId(value: UUID = UUID.randomUUID()) extends PK

object AccountId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, AccountId](AccountId.apply)
}

final case class AccountUsername(value: UsernameString)

object AccountUsername {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountUsername, UsernameString](_.value)

  implicit val decode = MappedEncoding[UsernameString, AccountUsername](AccountUsername.apply)

}

final case class AccountEmail(value: EmailString)

object AccountEmail {

  def unsafeFrom(v: String): AccountEmail =
    AccountEmail(RefType.applyRef[EmailString].unsafeFrom(v))

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountEmail, EmailString](_.value)

  implicit val decode = MappedEncoding[EmailString, AccountEmail](AccountEmail.apply)
}

/**
  *
  * @param id An UUID that uniquely identifies a user.
  * @param username An unique username.
  * @param email An unique email.
  * @param firstname First name.
  * @param lastname Last name.
  */
final case class AccountTable(id: AccountId,
                              username: AccountUsername,
                              email: AccountEmail,
                              firstname: NonEmptyString,
                              lastname: NonEmptyString,
                              credentialId: Option[CredentialId])
    extends TableWithPK[AccountId]
    with Timestamped {

  val pk: AccountId = id
}
