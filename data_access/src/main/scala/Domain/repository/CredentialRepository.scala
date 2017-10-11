package Domain.repository

import javax.inject.{Inject, Singleton}
import DAL.DAO.{AccountDAO, CredentialDAO}
import DAL.DbContext
import DAL.table.{AccountTable, AccountUsername, CredentialId, CredentialTable}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CredentialRepository @Inject()(val ctx: DbContext,
                                     accountDAO: AccountDAO,
                                     credentialDAO: CredentialDAO)(implicit ec: ExecutionContext)
    extends DelegableAuthInfoDAO[PasswordInfo] {
  import ctx._
  import CredentialRepository._

  val table = quote(querySchema[AccountTable]("account"))
  val ctable = quote(querySchema[CredentialTable]("credential"))

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    run(quote {
      table
        .filter(_.username == lift(AccountUsername(loginInfo.providerKey)))
        .flatMap(account => ctable.filter(v => account.credentialId.contains(v.id)))
    }).map(_.headOption.map(credentialTableToPasswordInfo))

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo The auth info to add.
    * @return The added auth info.
    */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    ???
//    ctx.transaction { implicit tec =>
//      for {
//        account <- accountDAO.findBy(AccountUsername(loginInfo.providerKey))
//        acc <- account match {
//                // FIXME: Maybe make up a custom exception for user not found?
//                // Retrieving user could return none, in which cases an user not found exception is thrown
//                case None    => Future.failed(new Exception("User not found"))
//                case Some(v) => Future.successful(v)
//              }
//        cred <- credentialDAO.insert(
//                 CredentialTable(authInfo.hasher, authInfo.password, authInfo.salt))
//        _ <- accountDAO.update(acc.id, cred.id)
//      } yield authInfo
//    }
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo The auth info to update.
    * @return The updated auth info.
    */
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    ???
//    ctx.transaction { implicit tec =>
//      for {
//        account <- accountDAO.findBy(AccountUsername(loginInfo.providerKey))
//        id <- account.flatMap(_.credentialId) match {
//               // FIXME: Maybe make up a custom exception for user not found?
//               // Retrieving user could return none, in which cases an user not found exception is thrown
//               case None    => Future.failed(new Exception("User does not have a password yet!"))
//               case Some(v) => Future.successful(v)
//             }
//        _ <- credentialDAO.updatePassword(id, authInfo.hasher, authInfo.password, authInfo.salt)
//      } yield authInfo
//    }
  }

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo The auth info to save.
    * @return The saved auth info.
    */
  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    // TODO: if only the save function is called, then add and update function can be optimized, possibly. This function needs optimization too.
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None    => add(loginInfo, authInfo)
    }
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  override def remove(loginInfo: LoginInfo): Future[Unit] = ???
}

object CredentialRepository {

  private def credentialTableToPasswordInfo(v: CredentialTable): PasswordInfo =
    PasswordInfo(v.hasher, v.hashedPassword, v.salt)

}
