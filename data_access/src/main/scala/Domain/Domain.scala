import java.util.UUID

package Domain {

  package service {

    import javax.inject.{Inject, Singleton}

    import DAL.table.{AccountTable, AccountUsername}
    import entity.{User, UserId}
    import Domain.repository._
    import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Silhouette}
    import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
    import com.mohiva.play.silhouette.api.util.{
      Clock,
      Credentials,
      PasswordHasherRegistry,
      PasswordInfo
    }
    import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
    import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
    import play.api.mvc.AnyContent
    import play.mvc.Http.RequestHeader
    import shapeless.tag.@@

    import scala.concurrent.Future
    import scala.concurrent.duration.FiniteDuration

    /**
      * Handles actions to auth tokens.
      *
    //  * @param authTokenDAO The auth token DAO implementation.
//  * @param clock        The clock instance.
//  * @param ex           The execution context.
      */
    @Singleton
    class AuthTokenService @Inject()(
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
    import play.api.mvc.Request

    final case class RememberMeConfig(cookieMaxAge: FiniteDuration,
                                      authenticatorIdleTimeout: FiniteDuration,
                                      authenticatorExpiry: FiniteDuration)

  }

  package repository {

    import javax.inject.{Inject, Singleton}

    import DAL.DAO.{AccountDAO, AuthTokenDAO, CredentialDAO}
    import DAL.DbContext
    import DAL.table._
    import Domain.entity._
    import com.mohiva.play.silhouette.api._
    import com.mohiva.play.silhouette.api.services.IdentityService
    import com.mohiva.play.silhouette.api.util.{Clock, PasswordInfo}
    import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
    import org.joda.time.DateTimeZone
    import play.api.mvc.{AnyContent, Request, RequestHeader}
    import shapeless.tag
    import tag._

    import scala.concurrent.{ExecutionContext, Future}

    trait DefaultEnv extends Env {
      type I = User
      type A = CookieAuthenticator
    }

    @Singleton
    class AccountEventBus @Inject()(silhouette: Silhouette[DefaultEnv]) {

      def publishSignUpEvent[I <: Identity](identity: I,
                                            request: play.api.mvc.RequestHeader): Future[Unit] = {
        Future.successful(silhouette.env.eventBus.publish(SignUpEvent(identity, request)))
      }

      def publishSignInEvent[I <: Identity](identity: I,
                                            request: Request[AnyContent]): Future[Unit] = {
        Future.successful(silhouette.env.eventBus.publish(LoginEvent(identity, request)))
      }
    }

    @Singleton
    class UserRepository {}

    @Singleton
    class AccountRepository @Inject()(val ctx: DbContext,
                                      accountDAO: AccountDAO,
                                      credentialDAO: CredentialDAO)(implicit ec: ExecutionContext)
        extends IdentityService[User] {
      import AccountRepository._
      import ctx._

      /**
        * Retrieves a user that matches the specified ID.
        *
        * @param id The ID to retrieve a user.
        * @return The retrieved user or None if no user could be retrieved for the given ID.
        */
      def retrieve(id: UUID) = accountDAO.findBy(AccountId(id))

      /**
        * Retrieves a user that matches the specified login info.
        *
        * @param loginInfo The login info to retrieve a user.
        * @return The retrieved user or None if no user could be retrieved for the given login info.
        */
      def retrieve(loginInfo: LoginInfo): Future[Option[User]] =
        for {
          account <- accountDAO.findBy(AccountUsername(loginInfo.providerKey))
          user = account.map(accountTableToUser)
        } yield user

      def createUser(username: String,
                     email: String,
                     firstname: String,
                     lastname: String,
                     passwordInfo: PasswordInfo,
                     loginInfo: LoginInfo): Future[User] = {
        ctx
          .transaction[AccountTable] { implicit c =>
            for {
              c <- credentialDAO.insert(
                    CredentialTable(
                      CredentialId(UUID.randomUUID()),
                      passwordInfo.hasher,
                      passwordInfo.password,
                      passwordInfo.salt
                    ))
              a <- accountDAO.insert(
                    AccountTable(
                      AccountId(UUID.randomUUID()),
                      AccountUsername(username),
                      email,
                      firstname,
                      lastname,
                      Some(c.id)
                    ))
            } yield a

          }
          .map(accountTableToUser)
      }

    }

    object AccountRepository {

      private def accountTableToUser(v: AccountTable): User =
        User(
          tag[UserId][UUID](v.id.value),
          v.username.value,
          v.email,
          v.firstname,
          v.lastname
        )
    }

    @Singleton
    class AuthTokenRepository @Inject()(authTokenDAO: AuthTokenDAO, clock: Clock) {

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
        val token = AuthToken(
          tag[entity.AuthTokenId][UUID](UUID.randomUUID()),
          userID,
          clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
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

    sealed trait Hasher
    case object BCryptSHA256

    final case class Password(hasher: Hasher, password: String, salt: Option[String] = None)

    sealed trait Provider
    case object Credentials

    final case class Login(provider: Provider, providerKey: String)

//    final case class Account()

  }
}
