package DAL.DAO

import DAL.DbContext
import DAL.table.{ProductId, ProductTable}
import javax.inject.{Inject, Singleton}
import monix.eval.Task
import scala.concurrent.{Future, ExecutionContext}

@Singleton
class ProductDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[ProductTable](_.id, _.createdAt)

  val table = quote(querySchema[ProductTable]("product"))

  def findAll: Task[Seq[ProductTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  def findByPk(pk: ProductId): Task[Option[ProductTable]] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(
          table
            .filter(_.id == lift(pk)))
      }
      .map(_.headOption)

  def insert(row: ProductTable): Future[ProductTable] =
    run(
      table.insert(lift(row))
    )
    .map(_ => row)

  def insertBatch(rows: Seq[ProductTable]): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: ProductTable): Task[ProductTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.update(lift(row)))
      }
      .map(_ => row)

  def updateBatch(rows: Seq[ProductTable]): Task[Long] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(quote {
          liftQuery(rows).foreach(v => table.update(v))
        })
      }
      .map(_.length)

  def deleteByPk(pk: ProductId): Task[Unit] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.filter(_.id == lift(pk)).delete)
      }
      .map(_ => ())

//  def insertBatch(products: Seq[ProductTable]): Task[Long] =
//    Task
//      .gatherUnordered(products.map(insert))
//      .map(_.length)
}
