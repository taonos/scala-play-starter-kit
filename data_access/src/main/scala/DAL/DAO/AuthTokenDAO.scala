package DAL.DAO

import javax.inject.{Inject, Singleton}
import DAL.DbContext
import DAL.table.{AuthTokenId, AuthTokenTable}
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthTokenDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext)
    extends DAOCrudWithPk[Future, AuthTokenTable, AuthTokenId] {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AuthTokenTable](_.id, _.createdAt)

  override val table = quote(querySchema[AuthTokenTable]("auth_token"))

  override def findAll: Future[Seq[AuthTokenTable]] =
    run(table)

  /**
    * Finds a token by its ID.
    *
    * @param pk The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  override def findByPk(pk: AuthTokenId): Future[Option[AuthTokenTable]] =
    run(
      table
        .filter(_.id == lift(pk)))
      .map(_.headOption)

  override def insert(row: AuthTokenTable): Future[AuthTokenTable] =
    run(table.insert(lift(row)))
      .map(_ => row)

  override def insertBatch(rows: Seq[AuthTokenTable]): Future[Long] = ???

  override def update(row: AuthTokenTable): Future[AuthTokenTable] = ???

  override def updateBatch(rows: Seq[AuthTokenTable]): Future[Long] = ???

  /**
    * Removes the token for the given ID.
    *
    * @param pk The ID for which the token should be removed.
    * @return A task
    */
  override def deleteByPk(pk: AuthTokenId): Future[Unit] =
    run(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())

  /**
    * Finds expired tokens.
    *
    * @param expiry The current date time.
    */
  def findByExpiry(expiry: DateTime) = ???
}
