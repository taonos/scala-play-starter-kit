package DAL.DAO

import DAL.table.{OwnershipId, OwnershipTable}
import scala.concurrent.ExecutionContext

trait OwnershipDAO extends DbContextable {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[OwnershipTable](_.createdAt)

  val table = quote(querySchema[OwnershipTable]("ownership"))

  def findAll(implicit ec: ExecutionContext): IO[Seq[OwnershipTable], Effect.Read] =
    runIO(table)

  def findByPk(
      pk: OwnershipId
  )(implicit ec: ExecutionContext): IO[Option[OwnershipTable], Effect.Read] =
    runIO(
      table
        .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
    ).map(_.headOption)

  def insert(row: OwnershipTable)(implicit ec: ExecutionContext): IO[OwnershipTable, Effect.Write] =
    runIO(table.insert(lift(row)))
      .map(_ => row)

  def insertBatch(
      rows: Seq[OwnershipTable]
  )(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
    IO.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      runIO(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: OwnershipTable)(implicit ec: ExecutionContext): IO[OwnershipTable, Effect.Write] =
    runIO(table.update(lift(row)))
      .map(_ => row)

  def updateBatch(
      rows: Seq[OwnershipTable]
  )(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
    runIO(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: OwnershipId)(implicit ec: ExecutionContext): IO[Unit, Effect.Write] =
    runIO(
      table
        .filter(v => v.id.accountId == lift(pk.accountId) && v.id.productId == lift(pk.productId))
        .delete
    ).map(_ => ())
}
