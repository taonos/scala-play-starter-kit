package DAL.table

import java.util.UUID
import DAL.DAO.{PK, TableWithPK}
import org.joda.time.DateTime

final case class AccountActivationTokenId(value: UUID) extends PK

object AccountActivationTokenId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[AccountActivationTokenId, UUID](_.value)
  implicit val decode =
    MappedEncoding[UUID, AccountActivationTokenId](AccountActivationTokenId.apply)
}

final case class AccountActivationTokenTable(id: AccountActivationTokenId,
                                             accountId: AccountId,
                                             expiry: DateTime)
    extends TableWithPK[AccountActivationTokenId]
    with Timestamped {

  val pk: AccountActivationTokenId = id
}
