package DAL.DAO

import DAL.table._
import scala.concurrent.ExecutionContext

trait AccountDAO extends DbContextable {
  import ctx._

  object AccountDAO {

    private implicit val updateExclusion =
      updateMeta[AccountTable](_.id, _.createdAt)

    val tableName: String = "account"

    val table = quote(querySchema[AccountTable]("account"))

    private val filterById = quote { (id: AccountId) =>
      table.filter(_.id == id)
    }

    private val filterByEmail = quote { (email: AccountEmail) =>
      table.filter(_.email == email)
    }

    def findAll(implicit ec: ExecutionContext): IO[Seq[AccountTable], Effect.Read] =
      runIO(table)

    def findBy(
        pk: AccountId
    )(implicit ec: ExecutionContext): IO[Option[AccountTable], Effect.Read] =
      runIO(
        filterById(lift(pk))
      ).map(_.headOption)

    def findBy(
        username: AccountUsername
    )(implicit ec: ExecutionContext): IO[Option[AccountTable], Effect.Read] =
      runIO(table.filter(_.username == lift(username)))
        .map(_.headOption)

    def findBy(
        email: AccountEmail
    )(implicit ec: ExecutionContext): IO[Option[AccountTable], Effect.Read] =
      runIO(filterByEmail(lift(email))).map(_.headOption)

    private val insertQuote = quote { (row: AccountTable) =>
      table.insert(row)
    }

    def insert(row: AccountTable)(implicit ec: ExecutionContext): IO[AccountTable, Effect.Write] =
      runIO(
        insertQuote(lift(row))
      ).map(_ => row)

    def insertBatch(
        rows: Seq[AccountTable]
    )(implicit ec: ExecutionContext): IO[Long, Effect.Write with Effect.Transaction] =
      runIO(quote {
        liftQuery(rows).foreach(v => table.insert(v))
      }).transactional
        .map(_.sum)
//      IO.sequence(rows.map(insert)).map(_.length)

    def update(row: AccountTable)(implicit ec: ExecutionContext): IO[AccountTable, Effect.Write] =
      runIO(table.update(lift(row)))
        .map(_ => row)

    def update(id: AccountId,
               column: CredentialId)(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
      runIO(filterById(lift(id)).update(_.credentialId -> lift(Option(column))))

    def updateBatch(
        rows: Seq[AccountTable]
    )(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
      runIO(quote {
        liftQuery(rows).foreach(v => table.update(v))
      }).map(_.length)

    def deleteByPk(pk: AccountId)(implicit ec: ExecutionContext): IO[Unit, Effect.Write] =
      runIO(table.filter(_.id == lift(pk)).delete)
        .map(_ => ())
  }
}
