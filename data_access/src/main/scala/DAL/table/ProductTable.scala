package DAL.table

import java.util.UUID

import DAL.DAO.{PK, TableWithPK}

final case class ProductId(value: UUID) extends PK
final case class ProductTable(id: ProductId, name: String)
    extends TableWithPK[ProductId]
    with Timestamped {
  val pk: ProductId = id
}

object ProductId {

  import io.getquill.MappedEncoding

  implicit val encodeProductId = MappedEncoding[ProductId, UUID](_.value)
  implicit val decodeProductId = MappedEncoding[UUID, ProductId](ProductId.apply)
}
