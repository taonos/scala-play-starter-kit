package DAL.DAO

import javax.inject.{Inject, Singleton}
import DAL.table.{OwnershipId, OwnershipTable}
import DAL.DbContext
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OwnershipDAO @Inject()(val ctx: DbContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[OwnershipTable](_.createdAt)

  val table = quote(querySchema[OwnershipTable]("ownership"))

  def findAll(implicit ec: ExecutionContext): Future[Seq[OwnershipTable]] =
    run(table)

  def findByPk(pk: OwnershipId)(implicit ec: ExecutionContext): Future[Option[OwnershipTable]] =
    run(
      table
        .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
    ).map(_.headOption)

  def insert(row: OwnershipTable)(implicit ec: ExecutionContext): Future[OwnershipTable] =
    run(table.insert(lift(row)))
      .map(_ => row)

  def insertBatch(rows: Seq[OwnershipTable])(implicit ec: ExecutionContext): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: OwnershipTable)(implicit ec: ExecutionContext): Future[OwnershipTable] =
    run(table.update(lift(row)))
      .map(_ => row)

  def updateBatch(rows: Seq[OwnershipTable])(implicit ec: ExecutionContext): Future[Long] =
    run(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: OwnershipId)(implicit ec: ExecutionContext): Future[Unit] =
    run(
      table
        .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
        .delete
    ).map(_ => ())
}
