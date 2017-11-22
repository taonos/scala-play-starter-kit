import DAL.DbContext
import DAL.DAO.{AccountDAO, CredentialDAO, OwnershipDAO, ProductDAO}
import DAL.table._
import eu.timepit.refined.api.RefType

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import java.util.UUID

import utility.RefinedTypes._
import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher
import cats.data.StateT

final class Repo extends CredentialDAO with ProductDAO with AccountDAO with OwnershipDAO {
  val ctx: DbContext = new DbContext()
  import ctx._
  import cats.implicits._

  private def insertionState(io: Future[Long], tableName: String)(implicit ec: ExecutionContext) =
    StateT[Future, Seq[String], Long] { seed =>
      val s = io.map(v => seed :+ s"Number of records inserted to `$tableName` table is $v")

      s zip io
    }

  def populate(
      cred: Seq[CredentialTable],
      acc: Seq[AccountTable],
      prod: Seq[ProductTable],
      own: Seq[OwnershipTable]
  )(implicit ec: ExecutionContext): Future[(Seq[String], Long)] = {

//    val res = for {
//      a <- CredentialDAO.insertBatch(cred)
//      b <- AccountDAO.insertBatch(acc)
//      c <- ProductDAO.insertBatch(prod)
//      d <- OwnershipDAO.insertBatch(own)
//    } yield a + b + c + d
//
//    performIO(res.transactional)

    ctx.transaction { implicit tec =>
      val res = for {
        a <- insertionState(performIO(CredentialDAO.insertBatch(cred)), CredentialDAO.tableName)
        b <- insertionState(performIO(AccountDAO.insertBatch(acc)), AccountDAO.tableName)
        c <- insertionState(performIO(ProductDAO.insertBatch(prod)), ProductDAO.tableName)
        d <- insertionState(performIO(OwnershipDAO.insertBatch(own)), OwnershipDAO.tableName)
      } yield a + b + c + d

      res.run(Seq(""))
    }(ec)
  }
}

object Main extends App {

  val hasher = new BCryptSha256PasswordHasher()
  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global

  val repo = new Repo()

  val credentialIds = Seq(
    CredentialId(UUID.fromString("c32cb561-4a12-4531-abb3-678cce62a103")),
    CredentialId(UUID.fromString("6a9e6978-6580-45a1-aac7-d0241e5070a7"))
  )

  val credentialDetails = Seq(
    hasher.hash("12345678"),
    hasher.hash("12345678")
  )

  val credentialEntities: Seq[CredentialTable] = credentialIds
    .zip(credentialDetails)
    .map {
      case (id, v) =>
        CredentialTable(
          Hasher.unsafeFrom(v.hasher),
          HashedPassword.unsafeFrom(v.password),
          v.salt,
          id
        )
    }

  val accountId = Seq(
    AccountId(UUID.fromString("36e79363-9c93-46f3-8de2-985d3b0d8a41")),
    AccountId(UUID.fromString("4454de99-dffa-4a68-a474-4fd3326dabd2"))
  )

  val userDetails = Seq(
    (
      AccountUsername.unsafeFrom("peter123"),
      AccountEmail.unsafeFrom("123@hotmail.com"),
      RefType.applyRef[NonEmptyString].unsafeFrom("Peter"),
      RefType.applyRef[NonEmptyString].unsafeFrom("Quill")
    ),
    (
      AccountUsername.unsafeFrom("wootwoot"),
      AccountEmail.unsafeFrom("345@hotmail"),
      RefType.applyRef[NonEmptyString].unsafeFrom("Mike"),
      RefType.applyRef[NonEmptyString].unsafeFrom("Json")
    )
  )

  val accountEntities = accountId
    .zip(userDetails)
    .zip(credentialIds)
    .map {
      case ((id, (username, email, firstname, lastname)), credentialId) =>
        AccountTable(id, username, email, firstname, lastname, Some(credentialId))
    }

  val productUUIDs = Seq(
    UUID.fromString("e2c789d0-8216-4258-bfdb-217f4824bc29"),
    UUID.fromString("221199fc-36ce-49bc-a889-c6ce21a4d45e")
  ).map(ProductId.apply)

  val productDetails = Seq(
    "Computer",
    "Apple"
  )

  val productEntities = productUUIDs
    .zip(productDetails)
    .map(v => ProductTable(v._1, v._2))

  val ownershipEntities = accountId
    .zip(productUUIDs)
    .map(v => OwnershipTable(OwnershipId(v._1, v._2)))

  val result =
    repo.populate(credentialEntities, accountEntities, productEntities, ownershipEntities)

  val inserted = Await.result(result, Duration.Inf)
  println("Database population results:")
  inserted._1.foreach(println)
}
