package DAL.DAO

import DAL.DbContext
import DAL.table.{AccountTable, AccountId}
import javax.inject.{Inject, Singleton}
import monix.eval.Task

@Singleton
class AccountDAO @Inject()(val ctx: DbContext)
    extends DAOCrudWithPk[Task, AccountTable, AccountId] {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountTable](_.id, _.createdAt)

  override val table = quote(querySchema[AccountTable]("account"))

  override def findAll: Task[Seq[AccountTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  override def findByPk(pk: AccountId): Task[Option[AccountTable]] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(
          table
            .filter(_.id == lift(pk)))
      }
      .map(_.headOption)

  private val insertQuote = quote { (row: AccountTable) =>
    table.insert(row)
  }

  override def insert(row: AccountTable): Task[AccountTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(
          insertQuote(lift(row))
        )
      }
      .map(_ => row)

  override def insertBatch(rows: Seq[AccountTable]): Task[Long] =
    Task.gatherUnordered(rows.map(insert)).map(_.length)
//    Task.deferFutureAction { implicit scheduler =>
//      run(quote {
//        liftQuery(rows).foreach(v => table.insert(v))
//      })
//    }
//      .map(_.length)

  override def update(row: AccountTable): Task[AccountTable] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.update(lift(row)))
      }
      .map(_ => row)

  override def updateBatch(rows: Seq[AccountTable]): Task[Long] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(quote {
          liftQuery(rows).foreach(v => table.update(v))
        })
      }
      .map(_.length)

  override def deleteByPk(pk: AccountId): Task[Unit] =
    Task
      .deferFutureAction { implicit scheduler =>
        run(table.filter(_.id == lift(pk)).delete)
      }
      .map(_ => ())
}
