package DAL.DAO

import DAL.table.{ProductId, ProductTable}
import scala.concurrent.ExecutionContext

trait ProductDAO extends DbContextable {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[ProductTable](_.id, _.createdAt)

  val table = quote(querySchema[ProductTable]("product"))

  def findAll(implicit ec: ExecutionContext): IO[Seq[ProductTable], Effect.Read] =
    runIO(table)

  def findBy(pk: ProductId)(implicit ec: ExecutionContext): IO[Option[ProductTable], Effect.Read] =
    runIO(
      table
        .filter(_.id == lift(pk))
    ).map(_.headOption)

  def insert(row: ProductTable)(implicit ec: ExecutionContext): IO[ProductTable, Effect.Write] =
    runIO(
      table.insert(lift(row))
    ).map(_ => row)

  def insertBatch(rows: Seq[ProductTable])(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
    IO.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      runIO(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: ProductTable)(implicit ec: ExecutionContext): IO[ProductTable, Effect.Write] =
    runIO(table.update(lift(row)))
      .map(_ => row)

  def updateBatch(rows: Seq[ProductTable])(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
    runIO(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: ProductId)(implicit ec: ExecutionContext): IO[Unit, Effect.Write] =
    runIO(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())

//  def insertBatch(products: Seq[ProductTable]): Task[Long] =
//    Task
//      .gatherUnordered(products.map(insert))
//      .map(_.length)
}
