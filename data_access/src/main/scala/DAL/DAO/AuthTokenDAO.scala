package DAL.DAO

import javax.inject.{Inject, Singleton}

import DAL.DbContext
import DAL.table.{AuthTokenId, AuthTokenTable}
import monix.eval.Task
import org.joda.time.DateTime

@Singleton
class AuthTokenDAO @Inject() (val ctx: DbContext) extends DAOCrudWithPk[Task, AuthTokenTable, AuthTokenId] {
  import ctx._


  private implicit val updateExclusion =
    updateMeta[AuthTokenTable](_.id, _.createdAt)

  override val table = quote(querySchema[AuthTokenTable]("auth_token"))

  override def findAll: Task[Seq[AuthTokenTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table)
    }

  /**
    * Finds a token by its ID.
    *
    * @param pk The unique token ID.
    * @return The found token or None if no token for the given ID could be found.
    */
  override def findByPk(pk: AuthTokenId): Task[Option[AuthTokenTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(table
        .filter(_.id == lift(pk)))
    }
      .map(_.headOption)

  override def insert(row: AuthTokenTable): Task[AuthTokenTable] =
    Task.deferFutureAction { implicit scheduler =>
      run(table.insert(lift(row)))
    }
      .map(_ => row)

  override def insertBatch(rows: Seq[AuthTokenTable]): Task[Long] = ???

  override def update(row: AuthTokenTable): Task[AuthTokenTable] = ???

  override def updateBatch(rows: Seq[AuthTokenTable]): Task[Long] = ???

  /**
    * Removes the token for the given ID.
    *
    * @param pk The ID for which the token should be removed.
    * @return A task
    */
  override def deleteByPk(pk: AuthTokenId): Task[Unit] =
    Task.deferFutureAction { implicit scheduler =>
      run(table.filter(_.id == lift(pk)).delete)
    }
    .map(_ => ())

  /**
    * Finds expired tokens.
    *
    * @param dateTime The current date time.
    */
  def findByExpiry(expiry: DateTime) = ???
}