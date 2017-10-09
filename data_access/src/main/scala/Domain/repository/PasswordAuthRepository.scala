package Domain.repository

import java.util.UUID
import javax.inject.Inject

import DAL.DAO.{AccountDAO, CredentialDAO}
import DAL.DbContext
import DAL.table.{AccountTable, AccountUsername, CredentialTable}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import monix.execution.Scheduler.Implicits.global

import scala.concurrent.{ExecutionContext, Future}

class PasswordAuthRepository @Inject()(val ctx: DbContext,
                                       accountDAO: AccountDAO,
                                       credentialDAO: CredentialDAO)
    extends DelegableAuthInfoDAO[PasswordInfo] {
  import ctx._
  import PasswordAuthRepository._

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
//      accountDAO.findBy(AccountUsername(loginInfo.providerKey))
//        .flatMap(v => credentialDAO.findBy(v))
      table
        .filter(_.username == lift(AccountUsername(loginInfo.providerKey)))
        .flatMap(account => ctable.filter(v => account.credentialId.contains(v.id)))
    }).map(_.headOption.map(credentialTableToPasswordInfo))

//    accountDAO.findBy(AccountId(UUID.fromString(loginInfo.providerKey)))
//      .map(
//        _.flatMap(v => for {
//          hasher <- v.hasher
//          password <- v.password
//        } yield PasswordInfo(hasher, password, v.salt))
//      )
//      .runAsync

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo The auth info to add.
    * @return The added auth info.
    */
  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo The auth info to update.
    * @return The updated auth info.
    */
  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = ???

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

object PasswordAuthRepository {

  private def credentialTableToPasswordInfo(v: CredentialTable): PasswordInfo =
    PasswordInfo(v.hasher, v.hashedPassword, v.salt)
}
