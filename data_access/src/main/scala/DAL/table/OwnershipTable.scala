package DAL.table

import DAL.DAO.{PK, TableWithPK}
import io.getquill.Embedded

final case class OwnershipId(accountId: AccountId, productId: ProductId) extends Embedded with PK
final case class OwnershipTable(id: OwnershipId) extends TableWithPK[OwnershipId] with Timestamped {
  val pk: OwnershipId = id
}
