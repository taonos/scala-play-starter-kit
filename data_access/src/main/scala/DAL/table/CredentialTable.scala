package DAL.table

import java.util.UUID

import DAL.DAO.{PK, TableWithPK}
import io.getquill.Embedded

final case class CredentialId(value: UUID) extends PK

object CredentialId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[CredentialId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, CredentialId](CredentialId.apply)
}

/**
  *
  * @param id A unique identifier.
  * @param hasher
  * @param hashedPassword
  * @param salt
  */
final case class CredentialTable(id: CredentialId,
                                 hasher: String,
                                 hashedPassword: String,
                                 salt: Option[String]) extends TableWithPK[CredentialId] with Timestamped {
  val pk: CredentialId = id
}
