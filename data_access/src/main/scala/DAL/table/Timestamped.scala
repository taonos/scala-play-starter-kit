package DAL.table

trait Timestamped {
  val createdAt: CreationTime = new CreationTime
  val updatedAt: LastUpdateTime = new LastUpdateTime
}
