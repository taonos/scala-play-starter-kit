package DAL.DAO

import DAL.DbContext
import javax.inject.{Inject, Singleton}
import DAL.table.{AccountCredentialTable, AccountId, AccountUsername}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountCredentialDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountCredentialTable](_.username, _.email)

  val table = quote(querySchema[AccountCredentialTable]("account_credential"))

  private val filterById = quote { (id: AccountId) =>
    table.filter(_.accountId == id)
  }

  private val filterByUsername = quote { (u: AccountUsername) =>
    table.filter(_.username == u)
  }

  private val filterByEmail = quote { (u: String) =>
    table.filter(_.email == u)
  }

  def findBy(pk: AccountId): Future[Option[AccountCredentialTable]] =
    run(filterById(lift(pk))).map(_.headOption)

  def findBy(email: String): Future[Option[AccountCredentialTable]] =
    run(filterByEmail(lift(email))).map(_.headOption)

//  def insert(row: AccountCredentialTable): Future[AccountCredentialTable] =
//    run(quote {
//      table.insert(lift(row))
//    }).map(_ => row)

  def update(row: AccountCredentialTable): Future[AccountCredentialTable] =
    run(quote {
      table.update(lift(row))
    }).map(_ => row)

  def updatePassword(id: AccountId,
                     hasher: String,
                     hashedPassword: String,
                     salt: Option[String]): Future[Long] =
    run(filterById(lift(id)).update({ v =>
      v.hasher -> lift(Option(hasher))
    }, { v =>
      v.hashedPassword -> lift(Option(hashedPassword))
    }, { v =>
      v.salt -> lift(salt)
    }))

  def deleteBy(pk: AccountId): Future[Unit] =
    run(filterById(lift(pk)).delete).map(_ => ())

  def deleteBy(email: String): Future[Unit] =
    run(filterByEmail(lift(email)).delete).map(_ => ())
}
