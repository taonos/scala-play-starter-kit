package DAL.DAO

import java.util.UUID
import javax.inject.{Inject, Singleton}
import monix.eval.Task
import DAL.table.{AccountUsername, OwnershipTable}
import DAL.DbContext

@Singleton
class OwnershipDAO @Inject()(val ctx: DbContext) extends DAO {
  import ctx._


  private implicit val updateExclusion =
    updateMeta[OwnershipTable](_.createdAt)


  private val ownershipQuery: Quoted[EntityQuery[OwnershipTable]] = quote(querySchema[OwnershipTable]("ownership"))


  def find(accountId: AccountUsername, productId: UUID): Task[Option[OwnershipTable]] =
    Task.deferFutureAction { implicit scheduler =>
      run(ownershipQuery
          .filter(v => v.accountUsername == lift(accountId) && v.productId == lift(productId))
      )
    }
      .map(_.headOption)

  def insert(ownership: OwnershipTable): Task[Long] =
    Task.deferFutureAction { implicit scheduler =>
      run(ownershipQuery.insert(lift(ownership)))
    }
}

case class AccountEntity()

//case class
//
//@Singleton
//class AccountRepository @Inject() (val accountDAO: AccountDAO) {
//  def changeName(firstname: String, lastname: String)
//}

@Singleton
class OwnershipRepository @Inject() (val ownershipDAO: OwnershipDAO) {

}

//case class Account

