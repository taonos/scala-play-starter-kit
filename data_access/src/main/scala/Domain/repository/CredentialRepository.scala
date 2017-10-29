package Domain.repository

import javax.inject.{Inject, Singleton}
import DAL.DAO.{AccountCredentialDAO}
import DAL.DbContext
import DAL.table._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CredentialRepository @Inject()(
    val ctx: DbContext,
    accountCredentialDAO: AccountCredentialDAO
)(implicit ec: ExecutionContext)
    extends DelegableAuthInfoDAO[PasswordInfo] {
  import CredentialRepository._

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] =
    accountCredentialDAO
      .findBy(loginInfo.providerKey)
      .map(_.flatMap(passwordInfoToAccountCredentialTable))

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo The auth info to add.
    * @return The added auth info.
    */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    _update(loginInfo: LoginInfo, authInfo: PasswordInfo)
  }

  private def _update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] =
    for {
      account <- accountCredentialDAO.findBy(loginInfo.providerKey)
      row = account.map(
        _.copy(
          credentialId = Option(new CredentialId),
          hasher = Option(authInfo.hasher),
          hashedPassword = Option(authInfo.password),
          salt = authInfo.salt
        )
      )
      _ <- row match {
            case None    => Future.failed(new IdentityNotFoundException("Account not found!"))
            case Some(v) => accountCredentialDAO.update(v)
          }
      res <- Future.successful(authInfo)
    } yield res

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo The auth info to update.
    * @return The updated auth info.
    */
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    _update(loginInfo: LoginInfo, authInfo: PasswordInfo)
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
  override def remove(loginInfo: LoginInfo): Future[Unit] =
    accountCredentialDAO.deleteBy(loginInfo.providerKey)
}

object CredentialRepository {

  private def passwordInfoToAccountCredentialTable(
      v: AccountCredentialTable
  ): Option[PasswordInfo] =
    for {
      hasher <- v.hasher
      pw <- v.hashedPassword
    } yield PasswordInfo(hasher, pw, v.salt)
}
