package DAL.DAO

import DAL.DbContext
import javax.inject.{Inject, Singleton}

import DAL.table._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountCredentialDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  val table = quote(querySchema[AccountCredentialTable]("account_credential"))

  private val filterById = quote { (id: AccountId) =>
    table.filter(_.accountId == id)
  }

  private val filterByUsername = quote { (u: AccountUsername) =>
    table.filter(_.username == u)
  }

  private val filterByEmail = quote { (u: AccountEmail) =>
    table.filter(_.email == u)
  }

  def existOne(email: AccountEmail): Future[Boolean] =
    run(filterByEmail(lift(email)).size).map {
      case 1 => true
      case _ => false
    }

  def findBy(pk: AccountId): Future[Option[AccountCredentialTable]] =
    run(filterById(lift(pk))).map(_.headOption)

  def findBy(email: AccountEmail): Future[Option[AccountCredentialTable]] =
    run(filterByEmail(lift(email))).map(_.headOption)
}
