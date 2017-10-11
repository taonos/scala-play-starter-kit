package DAL.DAO

trait TableWithPK[C <: PK] extends DAL.table.Table {
  val pk: C
}
