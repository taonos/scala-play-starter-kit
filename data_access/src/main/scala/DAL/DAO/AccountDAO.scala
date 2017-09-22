package DAL.DAO

import DAL.DbContext
import DAL.table.{AccountTable, AccountUsername}
import javax.inject.{Inject, Singleton}
import monix.eval.Task

@Singleton
class AccountDAO @Inject()(val ctx: DbContext) extends DAOCrudWithPk[Task, AccountTable, AccountUsername] {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountTable](_.username, _.createdAt)

  override val table = quote(querySchema[AccountTable]("account"))


  override def findAll: Task[Seq[AccountTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  override def findByPk(pk: AccountUsername): Task[Option[AccountTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table
        .filter(_.username == lift(pk))
      )
    }
      .map(_.headOption)

  private val insertQuote = quote { (row: AccountTable) =>
    table.insert(row)
  }

  override def insert(row: AccountTable): Task[AccountTable] =
    Task.deferFutureAction { implicit scheduler =>
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
    Task.deferFutureAction { implicit scheduler =>
      run(table.update(lift(row)))
    }
      .map(_ => row)

  override def updateBatch(rows: Seq[AccountTable]): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(quote {
        liftQuery(rows).foreach(v => table.update(v))
      })
    }
      .map(_.length)

  override def deleteByPk(pk: AccountUsername): Task[Unit] =
    Task.deferFutureAction { implicit scheduler =>
      run(table.filter(_.username == lift(pk)).delete)
    }
      .map(_ => ())
}
