package DAL.DAO

import DAL.DbContext
import DAL.table.{AccountId, AccountTable, AccountUsername, CredentialId}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountTable](_.id, _.createdAt)

  val table = quote(querySchema[AccountTable]("account"))

  private val filterById = quote { (id: AccountId) =>
    table.filter(_.id == id)
  }

  def findAll: Future[Seq[AccountTable]] =
    run(table)

  def findBy(pk: AccountId): Future[Option[AccountTable]] =
    run(
      filterById(lift(pk))
    ).map(_.headOption)

  def findBy(username: AccountUsername): Future[Option[AccountTable]] =
    run(table.filter(_.username == lift(username)))
      .map(_.headOption)

  private val insertQuote = quote { (row: AccountTable) =>
    table.insert(row)
  }

  def insert(row: AccountTable): Future[AccountTable] =
    run(
      insertQuote(lift(row))
    ).map(_ => row)

  def insertBatch(rows: Seq[AccountTable]): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)

  def update(row: AccountTable): Future[AccountTable] =
    run(table.update(lift(row)))
      .map(_ => row)

  def update(id: AccountId, column: CredentialId): Future[Long] =
    run(filterById(lift(id)).update(_.credentialId -> lift(Option(column))))

  def updateBatch(rows: Seq[AccountTable]): Future[Long] =
    run(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: AccountId): Future[Unit] =
    run(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())
}
