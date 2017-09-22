package DAL.table

import java.util.UUID

final case class OwnershipTable(accountUsername: AccountUsername, productId: UUID) extends Timestamped
