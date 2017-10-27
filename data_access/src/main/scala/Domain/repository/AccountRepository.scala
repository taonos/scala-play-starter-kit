package Domain.repository

import javax.inject.{Inject, Singleton}
import DAL.DAO.{AccountCredentialDAO, AccountDAO, CredentialDAO}
import DAL.DbContext
import DAL.table._
import Domain.entity.Account
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordInfo
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountRepository @Inject()(
    val ctx: DbContext,
    accountDAO: AccountDAO,
    credentialDAO: CredentialDAO,
    accountCredentialDAO: AccountCredentialDAO
)(implicit ec: ExecutionContext)
    extends IdentityService[Account] {
  import AccountRepository._
  import ctx._

//      /**
//        * Retrieves a user that matches the specified ID.
//        *
//        * @param id The ID to retrieve a user.
//        * @return The retrieved user or None if no user could be retrieved for the given ID.
//        */
//      def retrieve(id: UUID): Future[Option[Account]] = ???
////        accountCredentialDAO.findBy(AccountId(id))

  /**
    * Retrieves a user that matches the specified login info.
    *
    * @param loginInfo The login info to retrieve a user.
    * @return The retrieved user or None if no user could be retrieved for the given login info.
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[Account]] =
    for {
      account <- accountCredentialDAO.findBy(loginInfo.providerKey)
      r = account.map(accountCredentialTableToUser)
    } yield r

  def createUser(username: String,
                 email: String,
                 firstname: String,
                 lastname: String,
                 passwordInfo: PasswordInfo,
                 loginInfo: LoginInfo): Future[Account] = {
    ctx
      .transaction[AccountTable] { implicit c =>
        for {
          c <- credentialDAO.insert(
                CredentialTable(
                  passwordInfo.hasher,
                  passwordInfo.password,
                  passwordInfo.salt
                )
              )
          a <- accountDAO.insert(
                AccountTable(
                  new AccountId,
                  AccountUsername(username),
                  email,
                  firstname,
                  lastname,
                  Some(c.id)
                )
              )
        } yield a

      }
      .map(accountTableToUser)
  }

}

object AccountRepository {

  private def accountTableToUser(v: AccountTable): Account =
    Account(
      v.id.value,
      v.username.value,
      v.email
    )

  private def accountCredentialTableToUser(v: AccountCredentialTable): Account =
    Account(
      v.accountId.value,
      v.username.value,
      v.email
    )
}
