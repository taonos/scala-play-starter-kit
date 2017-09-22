package DAL.DAO

import java.util.UUID

import DAL.DbContext
import DAL.table.{ProductId, ProductTable}
import javax.inject.{Inject, Singleton}

import monix.eval.Task

@Singleton
class ProductDAO @Inject()(val ctx: DbContext) extends DAOCrudWithPk[Task, ProductTable, ProductId] {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[ProductTable](_.id, _.createdAt)

  override val table = quote(querySchema[ProductTable]("product"))

  override def findAll: Task[Seq[ProductTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  def findByPk(pk: ProductId): Task[Option[ProductTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table
        .filter(_.id == lift(pk))
      )
    }
      .map(_.headOption)

  override def insert(row: ProductTable): Task[ProductTable] =
    Task.deferFutureAction { implicit scheduler =>
      run(
        table.insert(lift(row))
      )
    }
    .map(_ => row)

  override def insertBatch(rows: Seq[ProductTable]): Task[Long] =
    Task.gatherUnordered(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  override def update(row: ProductTable): Task[ProductTable] =
    Task.deferFutureAction { implicit scheduler =>
      run(table.update(lift(row)))
    }
    .map(_ => row)

  override def updateBatch(rows: Seq[ProductTable]): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(quote {
        liftQuery(rows).foreach(v => table.update(v))
      })
    }
      .map(_.length)

  override def deleteByPk(pk: ProductId): Task[Unit] =
    Task.deferFutureAction { implicit scheduler =>
      run(table.filter(_.id == lift(pk)).delete)
    }
    .map(_ => ())

//  def insertBatch(products: Seq[ProductTable]): Task[Long] =
//    Task
//      .gatherUnordered(products.map(insert))
//      .map(_.length)
}
