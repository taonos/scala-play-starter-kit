package DAL.table

import java.util.UUID

import DAL.DAO.{PK, TableWithPK}
import io.getquill.Embedded

final case class CredentialId(value: UUID = UUID.randomUUID()) extends PK

object CredentialId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[CredentialId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, CredentialId](CredentialId.apply)
}

/**
  *
  * @param hasher
  * @param hashedPassword
  * @param salt
  * @param id A unique identifier.
  */
final case class CredentialTable(hasher: String,
                                 hashedPassword: String,
                                 salt: Option[String],
                                 id: CredentialId = new CredentialId)
    extends TableWithPK[CredentialId]
    with Timestamped {
  val pk: CredentialId = id
}
