package Domain.repository

import javax.inject.{Inject, Singleton}

import DAL.DAO.{AccountCredentialDAO, AccountDAO, CredentialDAO}
import DAL.DbContext
import DAL.table._
import Domain.entity.{Account, LoginProvider}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.api.util.PasswordInfo
import eu.timepit.refined.api.RefType

import scala.concurrent.{ExecutionContext, Future}
import utility.RefinedTypes.{EmailString, NonEmptyString, UsernameString}

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

  /**
    * Retrieves a user that matches the specified login info.
    *
    * @param loginInfo The login info to retrieve a user.
    * @return The retrieved user or None if no user could be retrieved for the given login info.
    */
  def retrieve(loginInfo: LoginInfo): Future[Option[Account]] =
    for {
      account <- accountCredentialDAO.findBy(loginInfoToAccountEmail(loginInfo))
      r = account.map(accountCredentialTableToUser(_, loginInfo))
    } yield r

  def createUser(username: UsernameString,
                 email: EmailString,
                 firstname: String,
                 lastname: String,
                 passwordInfo: PasswordInfo,
                 loginInfo: LoginInfo): Future[Account] = {
    ctx
      .transaction[AccountTable] { implicit c =>
        for {
          c <- credentialDAO.insert(passwordInfoToCredentialTable(passwordInfo))
          a <- accountDAO.insert(
                AccountTable(
                  new AccountId,
                  AccountUsername(username),
                  AccountEmail(email),
                  RefType.applyRef[NonEmptyString].unsafeFrom(firstname),
                  RefType.applyRef[NonEmptyString].unsafeFrom(lastname),
                  Some(c.id)
                )
              )
        } yield a

      }
      .map(accountTableToUser(_, loginInfo))
  }

}

object AccountRepository {

  @throws[IllegalArgumentException]
  private def accountTableToUser(v: AccountTable, l: LoginInfo): Account =
    Account(
      v.id.value,
      v.username.value,
      v.email.value,
      LoginProvider.unsafeFrom(l.providerID, l.providerKey)
    )

  @throws[IllegalArgumentException]
  private def accountCredentialTableToUser(v: AccountCredentialTable, l: LoginInfo): Account =
    Account(
      v.accountId.value,
      v.username.value,
      v.email.value,
      LoginProvider.unsafeFrom(l.providerID, l.providerKey)
    )

  @throws[IllegalArgumentException]
  private def loginInfoToAccountEmail(v: LoginInfo): AccountEmail =
    AccountEmail.unsafeFrom(v.providerKey)

  @throws[IllegalArgumentException]
  private def passwordInfoToCredentialTable(v: PasswordInfo): CredentialTable =
    CredentialTable(
      Hasher.withName(v.hasher),
      HashedPassword.unsafeFrom(v.password),
      v.salt
    )
}
