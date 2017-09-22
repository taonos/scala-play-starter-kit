package DAL.table

import java.util.UUID

//final case class ProductId(value: Long) extends AnyVal
final case class ProductTable(id: UUID, name: String) extends Timestamped