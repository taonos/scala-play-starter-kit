package DAL.DAO

import DAL.DbContext
import DAL.table.{AccountId, AccountTable, AccountUsername}
import javax.inject.{Inject, Singleton}

import monix.eval.Task

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountTable](_.id, _.createdAt)

  val table = quote(querySchema[AccountTable]("account"))

  def findAll: Task[Seq[AccountTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  def findBy(pk: AccountId): Future[Option[AccountTable]] =
    run(
      table
        .filter(_.id == lift(pk)))
    .map(_.headOption)

  def findBy(username: AccountUsername): Future[Option[AccountTable]] =
    run(table.filter(_.username == lift(username)))
    .map(_.headOption)

  private val insertQuote = quote { (row: AccountTable) =>
    table.insert(row)
  }

  def insert(row: AccountTable): Future[AccountTable] =
    run(
      insertQuote(lift(row))
    )
    .map(_ => row)

  def insertBatch(rows: Seq[AccountTable]): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  def update(row: AccountTable): Task[AccountTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.update(lift(row)))
      }
      .map(_ => row)

  def updateBatch(rows: Seq[AccountTable]): Task[Long] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(quote {
          liftQuery(rows).foreach(v => table.update(v))
        })
      }
      .map(_.length)

  def deleteByPk(pk: AccountId): Task[Unit] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.filter(_.id == lift(pk)).delete)
      }
      .map(_ => ())
}
