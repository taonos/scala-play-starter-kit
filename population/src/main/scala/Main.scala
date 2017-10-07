import DAL.DbContext
import DAL.DAO.{AccountDAO, CredentialDAO, OwnershipDAO, ProductDAO}
import DAL.table._

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import java.util.UUID

import com.mohiva.play.silhouette.password.BCryptSha256PasswordHasher


object Main extends App {

  val hasher = new BCryptSha256PasswordHasher()
  val ec = monix.execution.Scheduler.Implicits.global


  val ctx = new DbContext()
  import ctx._
  val credentialDAO = new CredentialDAO(ctx)(ec)
  val productDAO = new ProductDAO(ctx)(ec)
  val accountDAO = new AccountDAO(ctx)(ec)
  val ownershipDAO = new OwnershipDAO(ctx)(ec)

  val credentialIds = Seq(
    CredentialId(UUID.fromString("c32cb561-4a12-4531-abb3-678cce62a103")),
    CredentialId(UUID.fromString("6a9e6978-6580-45a1-aac7-d0241e5070a7"))
  )

  val credentialDetails = Seq(
    hasher.hash("12345678"),
    hasher.hash("12345678")
  )

  val credentialEntities = credentialIds
    .zip(credentialDetails)
    .map { case (id, v) =>
      CredentialTable(id, v.hasher, v.password, v.salt)
    }

  val accountId = Seq(
    AccountId(UUID.fromString("36e79363-9c93-46f3-8de2-985d3b0d8a41")),
    AccountId(UUID.fromString("4454de99-dffa-4a68-a474-4fd3326dabd2"))
  )

  val userDetails = Seq(
    (AccountUsername("peter123"), "123@hotmail.com", "Peter", "Quill"),
    (AccountUsername("wootwoot"), "345@hotmail", "Mike", "Json")
  )

  val accountEntities = accountId
    .zip(userDetails)
    .zip(credentialIds)
    .map { case ((id, (username, email, firstname, lastname)), credentialId) => AccountTable(id, username, email, firstname, lastname, Some(credentialId))}

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


  val result = ctx.transaction { implicit ec =>
    for {
      _ <- credentialDAO.insertBatch(credentialEntities)
      _ <- accountDAO.insertBatch(accountEntities)
      _ <- productDAO.insertBatch(productEntities)
      d <- ownershipDAO.insertBatch(ownershipEntities)
    } yield ()
  }(ec)

  val inserted = Await.result(result, Duration.Inf)
  println(s"Database population inserted with $inserted records.")
}
