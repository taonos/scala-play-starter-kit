package DAL.DAO

import DAL.DbContext
import DAL.table.{ProductId, ProductTable}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProductDAO @Inject()(val ctx: DbContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[ProductTable](_.id, _.createdAt)

  val table = quote(querySchema[ProductTable]("product"))

  def findAll(implicit ec: ExecutionContext): Future[Seq[ProductTable]] =
    run(table)

  def findBy(pk: ProductId)(implicit ec: ExecutionContext): Future[Option[ProductTable]] =
    run(
      table
        .filter(_.id == lift(pk))
    ).map(_.headOption)

  def insert(row: ProductTable)(implicit ec: ExecutionContext): Future[ProductTable] =
    run(
      table.insert(lift(row))
    ).map(_ => row)

  def insertBatch(rows: Seq[ProductTable])(implicit ec: ExecutionContext): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: ProductTable)(implicit ec: ExecutionContext): Future[ProductTable] =
    run(table.update(lift(row)))
      .map(_ => row)

  def updateBatch(rows: Seq[ProductTable])(implicit ec: ExecutionContext): Future[Long] =
    run(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: ProductId)(implicit ec: ExecutionContext): Future[Unit] =
    run(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())

//  def insertBatch(products: Seq[ProductTable]): Task[Long] =
//    Task
//      .gatherUnordered(products.map(insert))
//      .map(_.length)
}
