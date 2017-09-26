package DAL.DAO

import java.util.UUID
import javax.inject.{Inject, Singleton}

import monix.eval.Task
import DAL.table.{AccountUsername, OwnershipId, OwnershipTable, ProductId}
import DAL.DbContext

//trait HasIdentifier {
//  val id
//}

trait DbContextable {

  val ctx: DbContext
}

trait PK {}

trait TableWithPK[C <: PK] extends DAL.table.Table {
  val pk: C
}

trait DAOCrud[M[_], TB <: DAL.table.Table] extends DAO with DbContextable {
  import ctx._

  val table: Quoted[EntityQuery[TB]]

  def findAll: Task[Seq[TB]]

  def insert(row: TB): Task[TB]

  def insertBatch(rows: Seq[TB]): Task[Long]

  def update(row: TB): Task[TB]

  def updateBatch(rows: Seq[TB]): Task[Long]

  def delete(row: TB): Task[Unit]

//  def upsert(row: TB): Task[TB]

}

trait DAOCrudWithPk[M[_], TK <: TableWithPK[C], C <: PK] extends DAOCrud[M, TK] {

  def findByPk(pk: C): Task[Option[TK]]

  override def delete(row: TK): Task[Unit] =
    deleteByPk(row.pk)

  def deleteByPk(pk: C): Task[Unit]
}

@Singleton
class OwnershipDAO @Inject()(val ctx: DbContext)
    extends DAOCrudWithPk[Task, OwnershipTable, OwnershipId] {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[OwnershipTable](_.createdAt)

  override val table = quote(querySchema[OwnershipTable]("ownership"))

  override def findAll: Task[Seq[OwnershipTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  override def findByPk(pk: OwnershipId): Task[Option[OwnershipTable]] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(
          table
            .filter(v =>
              v.id.accountId == lift(pk.accountId) && v.id.productId == lift(
                pk.productId)))
      }
      .map(_.headOption)

  override def insert(row: OwnershipTable): Task[OwnershipTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.insert(lift(row)))
      }
      .map(_ => row)

  override def insertBatch(rows: Seq[OwnershipTable]): Task[Long] =
    Task.gatherUnordered(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  override def update(row: OwnershipTable): Task[OwnershipTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.update(lift(row)))
      }
      .map(_ => row)

  override def updateBatch(rows: Seq[OwnershipTable]): Task[Long] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(quote {
          liftQuery(rows).foreach(v => table.update(v))
        })
      }
      .map(_.length)

  override def deleteByPk(pk: OwnershipId): Task[Unit] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(
          table
            .filter(v =>
              v.id.accountId == lift(pk.accountId) && v.id.productId == lift(
                pk.productId))
            .delete)
      }
      .map(_ => ())
}

case class AccountEntity()

//case class
//
//@Singleton
//class AccountRepository @Inject() (val accountDAO: AccountDAO) {
//  def changeName(firstname: String, lastname: String)
//}

@Singleton
class OwnershipRepository @Inject()(val ownershipDAO: OwnershipDAO) {}

//case class Account
