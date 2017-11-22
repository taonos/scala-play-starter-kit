package DAL.DAO

import DAL.table._

import scala.concurrent.ExecutionContext

trait AccountCredentialDAO extends DbContextable {
  import ctx._

  object AccountCredentialDAO {

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

    def existOne(email: AccountEmail)(implicit ec: ExecutionContext): IO[Boolean, Effect.Read] =
      runIO(filterByEmail(lift(email)).size).map {
        case 1 => true
        case _ => false
      }

    def findBy(
        pk: AccountId
    )(implicit ec: ExecutionContext): IO[Option[AccountCredentialTable], Effect.Read] =
      runIO(filterById(lift(pk))).map(_.headOption)

    def findBy(
        email: AccountEmail
    )(implicit ec: ExecutionContext): IO[Option[AccountCredentialTable], Effect.Read] =
      runIO(filterByEmail(lift(email))).map(_.headOption)
  }
}
