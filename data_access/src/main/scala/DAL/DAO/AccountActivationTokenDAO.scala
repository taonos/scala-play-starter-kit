package DAL.DAO

import DAL.table.{AccountActivationTokenId, AccountActivationTokenTable}
import org.joda.time.DateTime
import scala.concurrent.ExecutionContext

trait AccountActivationTokenDAO extends DbContextable {
  import ctx._

  object AccountActivationTokenDAO {

    private implicit val updateExclusion =
      updateMeta[AccountActivationTokenTable](_.id, _.createdAt)

    val table = quote(querySchema[AccountActivationTokenTable]("account_activation_token"))

    def findAll(implicit ec: ExecutionContext): IO[Seq[AccountActivationTokenTable], Effect.Read] =
      runIO(table)

    /**
      * Finds a token by its ID.
      *
      * @param pk The unique token ID.
      * @return The found token or None if no token for the given ID could be found.
      */
    def findByPk(
        pk: AccountActivationTokenId
    )(implicit ec: ExecutionContext): IO[Option[AccountActivationTokenTable], Effect.Read] =
      runIO(
        table
          .filter(_.id == lift(pk))
      ).map(_.headOption)

    def insert(
        row: AccountActivationTokenTable
    )(implicit ec: ExecutionContext): IO[AccountActivationTokenTable, Effect.Write] =
      runIO(table.insert(lift(row)))
        .map(_ => row)

    def insertBatch(rows: Seq[AccountActivationTokenTable])(
        implicit ec: ExecutionContext
    ): IO[Long, Effect.Write] = ???

    def update(row: AccountActivationTokenTable)(
        implicit ec: ExecutionContext
    ): IO[AccountActivationTokenTable, Effect.Write] = ???

    def updateBatch(rows: Seq[AccountActivationTokenTable])(
        implicit ec: ExecutionContext
    ): IO[Long, Effect.Write] = ???

    /**
      * Removes the token for the given ID.
      *
      * @param pk The ID for which the token should be removed.
      * @return A task
      */
    def deleteByPk(
        pk: AccountActivationTokenId
    )(implicit ec: ExecutionContext): IO[Unit, Effect.Write] =
      runIO(table.filter(_.id == lift(pk)).delete)
        .map(_ => ())

    /**
      * Finds expired tokens.
      *
      * @param expiry The current date time.
      */
    def findByExpiry(expiry: DateTime)(implicit ec: ExecutionContext) = ???
  }
}
