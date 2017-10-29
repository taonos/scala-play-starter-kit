package DAL.table

import java.util.UUID

import eu.timepit.refined.api.RefType
import eu.timepit.refined._
import DAL.DAO.{PK, TableWithPK}
import utility.RefinedTypes.UsernameString

final case class AccountId(value: UUID = UUID.randomUUID()) extends PK

object AccountId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, AccountId](AccountId.apply)
}

final case class AccountUsername(value: UsernameString)

object AccountUsername {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountUsername, String](
    _.value.value
  )
  implicit val decode =
    MappedEncoding[String, AccountUsername] { str =>
      val value = RefType
        .applyRef[UsernameString](str)
        .map(AccountUsername.apply)
      value match {
        case Right(v) => v
        // TODO: come up with a better implementation
        case Left(_) => throw new Exception("Impossible path")
      }
    }
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
                              email: String,
                              firstname: String,
                              lastname: String,
                              credentialId: Option[CredentialId])
    extends TableWithPK[AccountId]
    with Timestamped {

  val pk: AccountId = id
}
