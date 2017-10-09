package DAL.DAO

import java.util.UUID
import javax.inject.{Inject, Singleton}

import monix.eval.Task
import DAL.table.{AccountUsername, OwnershipId, OwnershipTable, ProductId}
import DAL.DbContext

import scala.concurrent.{ExecutionContext, Future}

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

  def findAll: M[Seq[TB]]

  def insert(row: TB): M[TB]

  def insertBatch(rows: Seq[TB]): M[Long]

  def update(row: TB): M[TB]

  def updateBatch(rows: Seq[TB]): M[Long]

  def delete(row: TB): M[Unit]

//  def upsert(row: TB): Task[TB]

}

trait DAOCrudWithPk[M[_], TK <: TableWithPK[C], C <: PK] extends DAOCrud[M, TK] {

  def findByPk(pk: C): M[Option[TK]]

  override def delete(row: TK): M[Unit] =
    deleteByPk(row.pk)

  def deleteByPk(pk: C): M[Unit]
}

@Singleton
class OwnershipDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[OwnershipTable](_.createdAt)

  val table = quote(querySchema[OwnershipTable]("ownership"))

  def findAll: Task[Seq[OwnershipTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  def findByPk(pk: OwnershipId): Task[Option[OwnershipTable]] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(
          table
            .filter(v =>
              v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId)))
      }
      .map(_.headOption)

  def insert(row: OwnershipTable): Future[OwnershipTable] =
    run(table.insert(lift(row)))
      .map(_ => row)

  def insertBatch(rows: Seq[OwnershipTable]): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: OwnershipTable): Task[OwnershipTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.update(lift(row)))
      }
      .map(_ => row)

  def updateBatch(rows: Seq[OwnershipTable]): Task[Long] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(quote {
          liftQuery(rows).foreach(v => table.update(v))
        })
      }
      .map(_.length)

  def deleteByPk(pk: OwnershipId): Task[Unit] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table
          .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
          .delete)
      }
      .map(_ => ())
}
