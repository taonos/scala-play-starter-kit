package DAL.repository

import DAL.DbContext
import DAL.entity.ProductEntity
import javax.inject.{Inject, Singleton}
import monix.eval.Task

@Singleton
class ProductRepository @Inject() (val ctx: DbContext) extends Repository {
  import ctx._

  private implicit val insertExclusion =
    insertMeta[ProductEntity](_.id, _.created_at, _.updated_at)
  private implicit val updateExclusion =
    updateMeta[ProductEntity](_.id, _.created_at, _.updated_at)

  private val productQuery: Quoted[EntityQuery[ProductEntity]] = quote(querySchema[ProductEntity]("product"))

  def find(id: Int): Task[Option[ProductEntity]] =
    Task.deferFutureAction { implicit scheduler =>
      run(productQuery
        .filter(product => product.id == lift(id))
      )
    }
      .map(_.headOption)

  def all: Task[List[ProductEntity]] =
    Task.deferFutureAction { implicit scheduler => run(productQuery) }

  def create(product: ProductEntity): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(
        productQuery.insert(lift(product))
      )
    }

//  def batchCreate(products: Seq[ProductEntity]): Future[List[Long]] = {
//    val q = quote {
//      liftQuery(products).foreach(productQuery.insert)
//    }
//    run(q)
//  }
}
