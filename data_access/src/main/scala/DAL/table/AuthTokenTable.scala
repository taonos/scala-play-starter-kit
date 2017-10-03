package DAL.table

import java.util.UUID
import DAL.DAO.{PK, TableWithPK}
import org.joda.time.DateTime

final case class AuthTokenId(value: UUID) extends PK

object AuthTokenId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AuthTokenId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, AuthTokenId](AuthTokenId.apply)
}

final case class AuthTokenTable(id: AuthTokenId, accountId: AccountId, expiry: DateTime)
    extends TableWithPK[AuthTokenId]
    with Timestamped {

  val pk: AuthTokenId = id
}
