package DAL.table

import java.util.UUID
import DAL.DAO.{PK, TableWithPK}

final case class AccountId(value: UUID) extends PK
final case class AccountUsername(value: String)
final case class AccountTable(id: AccountId,
                              username: AccountUsername,
                              email: String,
                              firstname: String,
                              lastname: String)
    extends TableWithPK[AccountId]
    with Timestamped {

  val pk: AccountId = id
}

object AccountId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, AccountId](AccountId.apply)
}

object AccountUsername {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountUsername, String](_.value)
  implicit val decode = MappedEncoding[String, AccountUsername](AccountUsername.apply)
}
