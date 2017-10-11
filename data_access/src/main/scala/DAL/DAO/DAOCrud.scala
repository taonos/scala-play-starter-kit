package DAL.DAO

trait DAOCrud[M[_], TB <: DAL.table.Table] extends DAO with DbContextable {
  import ctx._

  val table: Quoted[EntityQuery[TB]]

  def findAll: M[Seq[TB]]

  def insert(row: TB): M[TB]

  def insertBatch(rows: Seq[TB]): M[Long]

  def update(row: TB): M[TB]

  def updateBatch(rows: Seq[TB]): M[Long]

  def delete(row: TB): M[Unit]

  //  def upsert(row: TB): Task[TB]

}
