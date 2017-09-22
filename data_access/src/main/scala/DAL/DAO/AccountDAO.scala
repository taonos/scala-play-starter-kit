package DAL.DAO

import DAL.DbContext
import DAL.table.{AccountTable, AccountUsername}
import javax.inject.{Inject, Singleton}

import monix.eval.Task

@Singleton
class AccountDAO @Inject()(val ctx: DbContext) extends DAO {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountTable](_.username, _.createdAt)

  private val accountQuery: Quoted[EntityQuery[AccountTable]] = quote(querySchema[AccountTable]("account"))

  def find(username: AccountUsername): Task[Option[AccountTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(accountQuery
        .filter(_.username == lift(username))
      )
    }
      .map(_.headOption)

  def all: Task[List[AccountTable]] =
    Task.deferFutureAction { implicit scheduler => run(accountQuery) }

  def insert(account: AccountTable): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(
        accountQuery.insert(lift(account))
      )
    }

//  def batchCreate(accounts: Seq[AccountTable]): Future[List[Long]] = {
//    val q = quote {
//      liftQuery(accounts).foreach(e => accountQuery.insert(e))
//    }
//    run(q)
//  }
}
