package DAL.DAO

import javax.inject.{Inject, Singleton}
import DAL.table.{OwnershipId, OwnershipTable}
import DAL.DbContext
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OwnershipDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[OwnershipTable](_.createdAt)

  val table = quote(querySchema[OwnershipTable]("ownership"))

  def findAll: Future[Seq[OwnershipTable]] =
    run(table)

  def findByPk(pk: OwnershipId): Future[Option[OwnershipTable]] =
    run(
      table
        .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
    ).map(_.headOption)

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

  def update(row: OwnershipTable): Future[OwnershipTable] =
    run(table.update(lift(row)))
      .map(_ => row)

  def updateBatch(rows: Seq[OwnershipTable]): Future[Long] =
    run(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: OwnershipId): Future[Unit] =
    run(
      table
        .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
        .delete
    ).map(_ => ())
}
