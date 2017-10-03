
import java.util.UUID

package Domain {

  package service {

    import javax.inject.{Inject, Singleton}

    import DAL.table.{AccountTable, AccountUsername}
    import entity.{User, UserId}
    import Domain.repository._
    import com.mohiva.play.silhouette.api.LoginInfo
    import com.mohiva.play.silhouette.api.util.PasswordInfo
    import shapeless.tag.@@

    import scala.concurrent.Future
    /**
      * Handles actions to auth tokens.
      *
    //  * @param authTokenDAO The auth token DAO implementation.
//  * @param clock        The clock instance.
//  * @param ex           The execution context.
      */
    @Singleton
    class AuthTokenService @Inject() (
                                       authTokenRepo: AuthTokenRepository
                                     ) {

      import scala.concurrent.duration._

      /**
        * Creates a new auth token and saves it in the backing store.
        *
        * @param userID The user ID for which the token should be created.
        * @param expiry The duration a token expires.
        * @return The saved auth token.
        */
      def create(userID: UUID @@ UserId, expiry: FiniteDuration = 5.minutes) =
        authTokenRepo.create(userID, expiry)

      /**
        * Validates a token ID.
        *
        * @param id The token ID to validate.
        * @return The token if it's valid, None otherwise.
        */
      def validate(id: UUID @@ UserId) = authTokenRepo.validate(id)
    }



    import com.mohiva.play.silhouette.api.services.IdentityService
    import shapeless.tag
    import DAL.DAO.AccountDAO
    import DAL.table.AccountId
    import Domain.entity.UserId
    /**
      * Handles actions to users.
      *
      * @param userRepo The User repository.
//  * @param ex      The execution context.
      */
    @Singleton
    class UserService @Inject() (userRepo: UserRepository) {
      import monix.execution.Scheduler.Implicits.global
      /**
        * Retrieves a user that matches the specified ID.
        *
        * @param id The ID to retrieve a user.
        * @return The retrieved user or None if no user could be retrieved for the given ID.
        */
      def retrieve(id: UUID) = userRepo.retrieve(id)

      /**
        * Retrieves a user that matches the specified login info.
        *
        * @param loginInfo The login info to retrieve a user.
        * @return The retrieved user or None if no user could be retrieved for the given login info.
        */
      def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
        userRepo.retrieve(loginInfo)

      def createUser(username: String, email: String, firstname: String, lastname: String, passwordInfo: PasswordInfo, loginInfo: LoginInfo): Future[User] =
        userRepo.createUser(username, email, firstname, lastname, passwordInfo, loginInfo)
    }

  }

  package repository {

    import javax.inject.{Inject, Singleton}

    import DAL.DAO.{AccountDAO, AuthTokenDAO}
    import DAL.table._
    import Domain.entity._
    import com.mohiva.play.silhouette.api.LoginInfo
    import com.mohiva.play.silhouette.api.services.IdentityService
    import com.mohiva.play.silhouette.api.util.{Clock, PasswordInfo}
    import org.joda.time.DateTimeZone
    import shapeless.tag
    import tag._

    import scala.concurrent.Future

    @Singleton
    class UserRepository @Inject() (accountDAO: AccountDAO) extends IdentityService[User] {
      import monix.execution.Scheduler.Implicits.global
      import UserRepository._

      /**
        * Retrieves a user that matches the specified ID.
        *
        * @param id The ID to retrieve a user.
        * @return The retrieved user or None if no user could be retrieved for the given ID.
        */
      def retrieve(id: UUID) = accountDAO.findByPk(AccountId(id)).runAsync

      /**
        * Retrieves a user that matches the specified login info.
        *
        * @param loginInfo The login info to retrieve a user.
        * @return The retrieved user or None if no user could be retrieved for the given login info.
        */
      def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
        accountDAO.findBy(loginInfo.providerID, loginInfo.providerKey)
          .map(_.map(accountTableToUser))
          .runAsync

      def createUser(username: String, email: String, firstname: String, lastname: String, passwordInfo: PasswordInfo, loginInfo: LoginInfo): Future[User] =
        accountDAO
          .insert(AccountTable(
            AccountId(UUID.randomUUID()),
            AccountUsername(username),
            email,
            firstname,
            lastname,
            passwordInfo.hasher,
            passwordInfo.password,
            passwordInfo.salt,
            loginInfo.providerID,
            loginInfo.providerKey
          ))
          .map(accountTableToUser)
          .runAsync

      //  /**
      //    * Saves the social profile for a user.
      //    *
      //    * If a user exists for this profile then update the user, otherwise create a new user with the given profile.
      //    *
      //    * @param profile The social profile to save.
      //    * @return The user for whom the profile was saved.
      //    */
      //  def save(profile: CommonSocialProfile) = {
      //    accountDAO.find(profile.loginInfo).flatMap {
      //      case Some(user) => // Update user with profile
      //        accountDAO.save(user.copy(
      //          firstName = profile.firstName,
      //          lastName = profile.lastName,
      //          fullName = profile.fullName,
      //          email = profile.email,
      //          avatarURL = profile.avatarURL
      //        ))
      //      case None => // Insert a new user
      //        accountDAO.save(User(
      //          userID = UUID.randomUUID(),
      //          loginInfo = profile.loginInfo,
      //          firstName = profile.firstName,
      //          lastName = profile.lastName,
      //          fullName = profile.fullName,
      //          email = profile.email,
      //          avatarURL = profile.avatarURL,
      //          activated = true
      //        ))
      //    }
      //  }

    }

    object UserRepository {

      private def accountTableToUser(v: AccountTable): User =
        User(tag[UserId][UUID](v.id.value), v.username.value, v.email, v.firstname, v.lastname, PasswordInfo(v.hasher, v.password, v.salt), LoginInfo(v.providerId, v.providerKey))
    }

    @Singleton
    class AuthTokenRepository @Inject() (authTokenDAO: AuthTokenDAO,
                                         clock: Clock) {

      import AuthTokenRepository._
      import scala.concurrent.duration._

      /**
        * Creates a new auth token and saves it in the backing store.
        *
        * @param userID The user ID for which the token should be created.
        * @param expiry The duration a token expires.
        * @return The saved auth token.
        */
      def create(userID: UUID @@ UserId, expiry: FiniteDuration = 5.minutes) = {
        val token = AuthToken(tag[entity.AuthTokenId][UUID](UUID.randomUUID()), userID, clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
        authTokenDAO.insert(authTokenToAuthTable(token))
      }

      /**
        * Validates a token ID.
        *
        * @param id The token ID to validate.
        * @return The token if it's valid, None otherwise.
        */
      def validate(id: UUID @@ UserId) = authTokenDAO.findByPk(DAL.table.AuthTokenId(id: UUID))

    }

    object AuthTokenRepository {
      private def authTokenToAuthTable(v: AuthToken) = {
        AuthTokenTable(AuthTokenId(v.id: UUID), AccountId(v.userID: UUID), v.expiry)
      }
    }
  }

  package entity {

    import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
    import com.mohiva.play.silhouette.api.util.PasswordInfo
    import org.joda.time.DateTime
    import shapeless.tag._

    sealed trait AuthTokenId

    /**
      * A token to authenticate a user against an endpoint for a short time period.
      *
      * @param id The unique token ID.
      * @param userID The unique ID of the user the token is associated with.
      * @param expiry The date-time the token expires.
      */
    final case class AuthToken(id: UUID @@ AuthTokenId, userID: UUID @@ UserId, expiry: DateTime)



    final case class Password()

    final case class Login()

    sealed trait UserId

    // TODO: May not need to have LoginInfo explicitly since it can be derived from id and providerId
    final case class User(id: UUID @@ UserId,
                          username: String,
                          email: String,
                          firstname: String,
                          lastname: String,
                          passwordInfo: PasswordInfo,
                          loginInfo: LoginInfo)
        extends Identity

  }
}
