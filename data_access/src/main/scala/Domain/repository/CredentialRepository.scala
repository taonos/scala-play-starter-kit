package Domain.repository

import javax.inject.{Inject, Singleton}

import DAL.DAO.{AccountDAO, CredentialDAO}
import DAL.DbContext
import DAL.table._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CredentialRepository @Inject()(val ctx: DbContext)(
    implicit ec: ExecutionContext
) extends DelegableAuthInfoDAO[PasswordInfo]
    with AccountDAO
    with CredentialDAO {
  import CredentialRepository._
  import ctx._

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val res = AccountDAO
      .findBy(loginInfoToAccountEmail(loginInfo))
      .map { acc =>
        acc.flatMap(v => v.credentialId)
      }
      .flatMap {
        case Some(id) => CredentialDAO.findBy(id)
        case None     => IO.successful(None)
      }
      .map(_.map(credentialTableToPasswordInfo))

    performIO(res)
  }

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo The auth info to add.
    * @return The added auth info.
    */
  @deprecated("user `save` to add or update password")
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    save(loginInfo: LoginInfo, authInfo: PasswordInfo)
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo The auth info to update.
    * @return The updated auth info.
    */
  @deprecated("user `save` to add or update password")
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    save(loginInfo: LoginInfo, authInfo: PasswordInfo)
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

    val res = AccountDAO
      .findBy(loginInfoToAccountEmail(loginInfo))
      .map { acc =>
        (acc.map(_.id), acc.flatMap(v => v.credentialId))
      }
      .flatMap {
        // if the user has a password already, update it.
        case (Some(_), Some(cid)) =>
          CredentialDAO
            .updatePassword(
              cid,
              Hasher.withName(authInfo.hasher),
              HashedPassword.unsafeFrom(authInfo.password),
              authInfo.salt
            )
            .map(_ => authInfo)
        // if the user does not have a password, insert a new credential and update account
        case (Some(aid), None) =>
          CredentialDAO
            .insert(passwordInfoToCredentialTable(authInfo))
            .flatMap(v => AccountDAO.update(aid, v.id))
            .map(_ => authInfo)
            .transactional
        case _ => IO.failed(new IdentityNotFoundException("User not found!"))
      }

    performIO(res)
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  override def remove(loginInfo: LoginInfo): Future[Unit] = {

    val res = AccountDAO
      .findBy(loginInfoToAccountEmail(loginInfo))
      .map { acc =>
        acc.flatMap(v => v.credentialId)
      }
      .flatMap {
        case None     => IO.failed(new IdentityNotFoundException("User not found!"))
        case Some(id) => CredentialDAO.deleteBy(id)
      }
      .transactional

    performIO(res)
  }
}

object CredentialRepository {

  import eu.timepit.refined.auto._

  private def credentialTableToPasswordInfo(v: CredentialTable): PasswordInfo =
    PasswordInfo(v.hasher.entryName, v.hashedPassword.value, v.salt)

  private def passwordInfoToCredentialTable(v: PasswordInfo): CredentialTable =
    CredentialTable(Hasher.withName(v.hasher), HashedPassword.unsafeFrom(v.password), v.salt)

  private def loginInfoToAccountEmail(v: LoginInfo): AccountEmail =
    AccountEmail.unsafeFrom(v.providerKey)
}
