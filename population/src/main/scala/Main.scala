import DAL.DbContext
import DAL.repository.{AccountRepository, ProductRepository}
import DAL.entity._
import scala.concurrent.duration.Duration
import scala.concurrent.Await
import monix.eval.Task
import monix.execution.Scheduler.Implicits.global

object Main extends App {

  val ctx = new DbContext()
  val productRepo = new ProductRepository(ctx)
  val accountRepo = new AccountRepository(ctx)

  val accountEntities = Seq(
    AccountEntity("Peter", "Quill"),
    AccountEntity("Mike", "Json")
  )

  val productEntities = Seq(
    ProductEntity("Computer"),
    ProductEntity("Apple")
  )

  val result = for {
    a <- Task.gatherUnordered(accountEntities.map(accountRepo.create)).map(_.length)
    b <- Task.gatherUnordered(productEntities.map(productRepo.create)).map(_.length)
  } yield a + b

  val inserted = Await.result(result.runAsync, Duration.Inf)
  println(s"Database population inserted with $inserted records.")

//  val result = for {
//    a <- accountRepo.batchCreate(accountEntities)
//    b <- productRepo.batchCreate(productEntities)
//  } yield (a, b)
//
//  val inserted = Await.result(result, Duration.Inf)
//  println(s"Database population inserted with $inserted records.")
}