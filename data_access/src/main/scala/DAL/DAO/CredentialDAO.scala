package DAL.DAO

import javax.inject.{Inject, Singleton}

import DAL.DbContext
import DAL.table.{CredentialId, CredentialTable}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CredentialDAO @Inject()(val ctx: DbContext)(implicit ec: ExecutionContext) {
  import ctx._

  private implicit val updateExclusion =
    updateMeta[CredentialTable](_.id, _.createdAt)

  val table = quote(querySchema[CredentialTable]("credential"))

  def findBy(pk: CredentialId): Future[Option[CredentialTable]] =
    run(table.filter(_.id == lift(pk))).map(_.headOption)

  def insert(row: CredentialTable): Future[CredentialTable] =
    run(table.insert(lift(row))).map(_ => row)

  def insertBatch(rows: Seq[CredentialTable]): Future[Long] =
    Future.sequence(rows.map(insert)).map(_.length)

  def update(row: CredentialTable): Future[CredentialTable] =
    run(table.update(lift(row))).map(_ => row)
}
