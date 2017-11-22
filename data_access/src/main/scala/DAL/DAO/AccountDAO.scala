package DAL.DAO

import DAL.DbContext
import DAL.table._
import javax.inject.{Inject, Singleton}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountDAO @Inject()(val ctx: DbContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[AccountTable](_.id, _.createdAt)

  val table = quote(querySchema[AccountTable]("account"))

  private val filterById = quote { (id: AccountId) =>
    table.filter(_.id == id)
  }

  private val filterByEmail = quote { (email: AccountEmail) =>
    table.filter(_.email == email)
  }

  def findAll(implicit ec: ExecutionContext): Future[Seq[AccountTable]] =
    run(table)

  def findBy(pk: AccountId)(implicit ec: ExecutionContext): Future[Option[AccountTable]] =
    run(
      filterById(lift(pk))
    ).map(_.headOption)

  def findBy(
      username: AccountUsername
  )(implicit ec: ExecutionContext): Future[Option[AccountTable]] =
    run(table.filter(_.username == lift(username)))
      .map(_.headOption)

  def findBy(email: AccountEmail)(implicit ec: ExecutionContext): Future[Option[AccountTable]] =
    run(filterByEmail(lift(email))).map(_.headOption)

  private val insertQuote = quote { (row: AccountTable) =>
    table.insert(row)
  }

  def insert(row: AccountTable)(implicit ec: ExecutionContext): Future[AccountTable] =
    run(
      insertQuote(lift(row))
    ).map(_ => row)

  def insertBatch(rows: Seq[AccountTable])(implicit ec: ExecutionContext): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)

  def update(row: AccountTable)(implicit ec: ExecutionContext): Future[AccountTable] =
    run(table.update(lift(row)))
      .map(_ => row)

  def update(id: AccountId, column: CredentialId)(implicit ec: ExecutionContext): Future[Long] =
    run(filterById(lift(id)).update(_.credentialId -> lift(Option(column))))

  def updateBatch(rows: Seq[AccountTable])(implicit ec: ExecutionContext): Future[Long] =
    run(quote {
      liftQuery(rows).foreach(v => table.update(v))
    }).map(_.length)

  def deleteByPk(pk: AccountId)(implicit ec: ExecutionContext): Future[Unit] =
    run(table.filter(_.id == lift(pk)).delete)
      .map(_ => ())
}
