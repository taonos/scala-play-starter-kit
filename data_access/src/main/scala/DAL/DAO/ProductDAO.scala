package DAL.DAO

import java.util.UUID
import DAL.DbContext
import DAL.table.ProductTable
import javax.inject.{Inject, Singleton}
import monix.eval.Task

@Singleton
class ProductDAO @Inject()(val ctx: DbContext) extends DAO {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[ProductTable](_.id, _.createdAt)

  private val productQuery: Quoted[EntityQuery[ProductTable]] = quote(querySchema[ProductTable]("product"))

  def find(id: UUID): Task[Option[ProductTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(productQuery
        .filter(_.id == lift(id))
      )
    }
      .map(_.headOption)

  def all: Task[List[ProductTable]] =
    Task.deferFutureAction { implicit scheduler => run(productQuery) }

  def insert(product: ProductTable): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(
        productQuery.insert(lift(product))
      )
    }

//  def batchCreate(products: Seq[ProductTable]): Future[List[Long]] = {
//    val q = quote {
//      liftQuery(products).foreach(productQuery.insert)
//    }
//    run(q)
//  }
}
