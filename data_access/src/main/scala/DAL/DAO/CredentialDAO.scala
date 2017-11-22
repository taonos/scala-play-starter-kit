package DAL.DAO

import DAL.table.{CredentialId, CredentialTable, HashedPassword, Hasher}

import scala.concurrent.ExecutionContext

trait CredentialDAO extends DbContextable {
  import ctx._

  object CredentialDAO {

    private implicit val updateExclusion =
      updateMeta[CredentialTable](_.id, _.createdAt)

    val table = quote(querySchema[CredentialTable]("credential"))

    private val filterById = quote { (id: CredentialId) =>
      table.filter(_.id == id)
    }

    def findBy(
        pk: CredentialId
    )(implicit ec: ExecutionContext): IO[Option[CredentialTable], Effect.Read] =
      runIO(filterById(lift(pk))).map(_.headOption)

    def insert(
        row: CredentialTable
    )(implicit ec: ExecutionContext): IO[CredentialTable, Effect.Write] =
      runIO(table.insert(lift(row))).map(_ => row)

    def insertBatch(
        rows: Seq[CredentialTable]
    )(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
      IO.sequence(rows.map(insert)).map(_.length)

    def update(
        row: CredentialTable
    )(implicit ec: ExecutionContext): IO[CredentialTable, Effect.Write] =
      runIO(table.update(lift(row))).map(_ => row)

    def updatePassword(
        id: CredentialId,
        hasher: Hasher,
        password: HashedPassword,
        salt: Option[String]
    )(implicit ec: ExecutionContext): IO[Long, Effect.Write] =
      runIO(
        filterById(lift(id))
          .update({ _.hasher -> lift(hasher) }, { _.hasher -> lift(hasher) }, {
            _.hashedPassword -> lift(password)
          })
      )

    def deleteBy(id: CredentialId)(implicit ec: ExecutionContext): IO[Unit, Effect.Write] =
      runIO(filterById(lift(id)).delete).map(_ => ())
  }
}
