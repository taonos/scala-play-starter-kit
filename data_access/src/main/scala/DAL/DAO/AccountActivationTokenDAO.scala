package DAL.DAO

import javax.inject.{Inject, Singleton}
import DAL.DbContext
import DAL.table.{AccountActivationTokenId, AccountActivationTokenTable}
import org.joda.time.DateTime
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountActivationTokenDAO @Inject()(val ctx: DbContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountActivationTokenTable](_.id, _.createdAt)

  val table = quote(querySchema[AccountActivationTokenTable]("account_activation_token"))

  def findAll(implicit ec: ExecutionContext): Future[Seq[AccountActivationTokenTable]] =
    run(table)

  /**
    * Finds a token by its ID.
    *
    * @param pk The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  def findByPk(
      pk: AccountActivationTokenId
  )(implicit ec: ExecutionContext): Future[Option[AccountActivationTokenTable]] =
    run(
      table
        .filter(_.id == lift(pk))
    ).map(_.headOption)

  def insert(
      row: AccountActivationTokenTable
  )(implicit ec: ExecutionContext): Future[AccountActivationTokenTable] =
    run(table.insert(lift(row)))
      .map(_ => row)

  def insertBatch(rows: Seq[AccountActivationTokenTable])(
      implicit ec: ExecutionContext
  ): Future[Long] = ???

  def update(row: AccountActivationTokenTable)(
      implicit ec: ExecutionContext
  ): Future[AccountActivationTokenTable] = ???

  def updateBatch(rows: Seq[AccountActivationTokenTable])(
      implicit ec: ExecutionContext
  ): Future[Long] = ???

  /**
    * Removes the token for the given ID.
    *
    * @param pk The ID for which the token should be removed.
    * @return A task
    */
  def deleteByPk(pk: AccountActivationTokenId)(implicit ec: ExecutionContext): Future[Unit] =
    run(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())

  /**
    * Finds expired tokens.
    *
    * @param expiry The current date time.
    */
  def findByExpiry(expiry: DateTime)(implicit ec: ExecutionContext) = ???
}
