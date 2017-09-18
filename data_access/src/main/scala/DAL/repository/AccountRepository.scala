package DAL.repository

import DAL.DbContext
import DAL.entity.AccountEntity
import javax.inject.{Inject, Singleton}
import monix.eval.Task

@Singleton
class AccountRepository @Inject() (val ctx: DbContext) extends Repository {
  import ctx._

  private implicit val insertExclusion =
    insertMeta[AccountEntity](_.id, _.created_at, _.updated_at)
  private implicit val updateExclusion =
    updateMeta[AccountEntity](_.id, _.created_at, _.updated_at)

  private val accountQuery: Quoted[EntityQuery[AccountEntity]] = quote(querySchema[AccountEntity]("account"))

  def find(id: Int): Task[Option[AccountEntity]] =
    Task.deferFutureAction { implicit scheduler =>
      run(accountQuery
        .filter(product => product.id == lift(id))
      )
    }
      .map(_.headOption)

  def all: Task[List[AccountEntity]] =
    Task.deferFutureAction { implicit scheduler => run(accountQuery) }

  def create(account: AccountEntity): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(
        accountQuery.insert(lift(account))
      )
    }

//  def batchCreate(accounts: Seq[AccountEntity]): Future[List[Long]] = {
//    val q = quote {
//      liftQuery(accounts).foreach(e => accountQuery.insert(e))
//    }
//    run(q)
//  }
}
