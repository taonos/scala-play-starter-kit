package DAL.DAO

trait DAOCrudWithPk[M[_], TK <: TableWithPK[C], C <: PK] extends DAOCrud[M, TK] {

  def findByPk(pk: C): M[Option[TK]]

  override def delete(row: TK): M[Unit] =
    deleteByPk(row.pk)

  def deleteByPk(pk: C): M[Unit]
}
