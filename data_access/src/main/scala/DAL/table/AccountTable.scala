package DAL.table

import DAL.DAO.{PK, TableWithPK}

final case class AccountUsername(value: String) extends PK
final case class AccountTable(username: AccountUsername, email: String, firstname: String, lastname: String)
  extends TableWithPK[AccountUsername] with Timestamped {

  val pk: AccountUsername = username
}

object AccountUsername {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountUsername, String](_.value)
  implicit val decode = MappedEncoding[String, AccountUsername](AccountUsername.apply)
}