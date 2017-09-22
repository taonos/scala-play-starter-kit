import DAL.DbContext
import DAL.DAO.{AccountDAO, OwnershipDAO, ProductDAO}
import DAL.table._

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import java.util.UUID

object Main extends App {

  val ctx = new DbContext()
  val productDAO = new ProductDAO(ctx)
  val accountDAO = new AccountDAO(ctx)
  val ownershipDAO = new OwnershipDAO(ctx)

  val usernames = Seq(
    AccountUsername("peter123"),
    AccountUsername("wootwoot")
  )

  val userDetails = Seq(
    ("123@hotmail.com", "Peter", "Quill"),
    ("345@hotmail", "Mike", "Json")
  )

  val accountEntities = usernames
    .zip(userDetails)
    .map { case (username, (email, firstname, lastname)) => AccountTable(username, email, firstname, lastname)}

  val productUUIDs = Seq(
    UUID.fromString("e2c789d0-8216-4258-bfdb-217f4824bc29"),
    UUID.fromString("221199fc-36ce-49bc-a889-c6ce21a4d45e")
  )

  val productDetails = Seq(
    "Computer",
    "Apple"
  )

  val productEntities = productUUIDs
    .zip(productDetails)
    .map(v => ProductTable(v._1, v._2))

  val ownershipEntities = usernames
    .zip(productUUIDs)
    .map(v => OwnershipTable(v._1, v._2))

  val result = for {
    a <- Task.gatherUnordered(accountEntities.map(accountDAO.insert)).map(_.length)
    b <- Task.gatherUnordered(productEntities.map(productDAO.insert)).map(_.length)
    c <- Task.gatherUnordered(ownershipEntities.map(ownershipDAO.insert)).map(_.length)
  } yield a + b + c

  val inserted = Await.result(result.runAsync, Duration.Inf)
  println(s"Database population inserted with $inserted records.")

//  val result = for {
//    a <- accountDAO.batchCreate(accountEntities)
//    b <- productDAO.batchCreate(productEntities)
//  } yield (a, b)
//
//  val inserted = Await.result(result, Duration.Inf)
//  println(s"Database population inserted with $inserted records.")
}