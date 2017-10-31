package DAL.DAO

import javax.inject.{Inject, Singleton}
import DAL.DbContext
import DAL.table.{AccountActivationTokenId, AccountActivationTokenTable}
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountActivationTokenDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext)
    extends DAOCrudWithPk[Future, AccountActivationTokenTable, AccountActivationTokenId] {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountActivationTokenTable](_.id, _.createdAt)

  override val table = quote(querySchema[AccountActivationTokenTable]("account_activation_token"))

  override def findAll: Future[Seq[AccountActivationTokenTable]] =
    run(table)

  /**
    * Finds a token by its ID.
    *
    * @param pk The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  override def findByPk(pk: AccountActivationTokenId): Future[Option[AccountActivationTokenTable]] =
    run(
      table
        .filter(_.id == lift(pk))
    ).map(_.headOption)

  override def insert(row: AccountActivationTokenTable): Future[AccountActivationTokenTable] =
    run(table.insert(lift(row)))
      .map(_ => row)

  override def insertBatch(rows: Seq[AccountActivationTokenTable]): Future[Long] = ???

  override def update(row: AccountActivationTokenTable): Future[AccountActivationTokenTable] = ???

  override def updateBatch(rows: Seq[AccountActivationTokenTable]): Future[Long] = ???

  /**
    * Removes the token for the given ID.
    *
    * @param pk The ID for which the token should be removed.
    * @return A task
    */
  override def deleteByPk(pk: AccountActivationTokenId): Future[Unit] =
    run(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())

  /**
    * Finds expired tokens.
    *
    * @param expiry The current date time.
    */
  def findByExpiry(expiry: DateTime) = ???
}
