package Domain.service

import javax.inject.{Inject, Singleton}

import Domain.entity.Account
import Domain.repository.{
  AccountActivationRepository,
  AccountEventBus,
  AccountRepository,
  CookieEnv
}
import Domain.service.AccountService.{Authenticated, InvalidPassword, SignInStatus, UserNotFound}
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.{
  IdentityNotFoundException,
  InvalidPasswordException
}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.mvc.{AnyContent, Request, Result}
import utility.RefinedTypes.{EmailString, UsernameString}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

final case class RememberMeConfig(cookieMaxAge: FiniteDuration,
                                  authenticatorIdleTimeout: FiniteDuration,
                                  authenticatorExpiry: FiniteDuration)

@Singleton
class AccountManager @Inject()(accountRepo: AccountRepository,
                               silhouette: Silhouette[CookieEnv],
                               passwordHasherRegistry: PasswordHasherRegistry,
//                               authInfoRepo: AuthInfoRepository,
                               accountActivationRepo: AccountActivationRepository,
                               credentialsProvider: CredentialsProvider,
                               clock: Clock,
                               rememberMeConfig: RememberMeConfig) {

  import Domain.entity.command.UserRegistrationByPassword
  import Domain.entity.AccountStatus
  import AccountStatus._
  import eu.timepit.refined.auto._

  def register(
      command: UserRegistrationByPassword
  )(implicit ec: ExecutionContext): Future[AccountStatus] = {

    val loginInfo = LoginInfo(credentialsProvider.id, command.email.value)

    for {
      userRetrieved <- accountRepo
        .retrieve(loginInfo)
      a = userRetrieved.fold[AccountStatus](NotRegistered)(_ => AlreadyExists)
      result <- a match {
        // save account info
        case NotRegistered =>
          val passwordInfo = passwordHasherRegistry.current.hash(command.password)

          for {
            account <- accountRepo.createUser(
              command.username,
              command.email,
              command.firstname.value,
              command.lastname.value,
              passwordInfo,
              loginInfo
            )
//            _ <- authInfoRepo.add(loginInfo, passwordInfo)
            authToken <- accountActivationRepo.create(account.id)
          } yield Registered(account)

        // account is already registered within the system
        case v => Future.successful(v)
      }
    } yield result

  }

  def signIn(email: String, password: String, rememberMe: Boolean, response: Result)(
      implicit ec: ExecutionContext,
      request: Request[AnyContent]
  ): Future[SignInStatus] = {
    val credentials = Credentials(email, password)

    val res = for {
      loginInfo <- credentialsProvider.authenticate(credentials)
      retrieved <- accountRepo.retrieve(loginInfo)
      user <- retrieved match {
        case None    => Future.failed(new IdentityNotFoundException("Account not found!"))
        case Some(v) => Future.successful(v)
      }
      cookieAuth <- silhouette.env.authenticatorService
        .create(loginInfo)
        .map {
          case authenticator if rememberMe =>
            authenticator.copy(
              expirationDateTime = clock.now + rememberMeConfig.authenticatorExpiry,
              idleTimeout = Some(rememberMeConfig.authenticatorIdleTimeout),
              cookieMaxAge = Some(rememberMeConfig.cookieMaxAge)
            )
          case authenticator => authenticator
        }
      cookie <- silhouette.env.authenticatorService.init(cookieAuth)
      authResult <- silhouette.env.authenticatorService.embed(cookie, response)
    } yield Authenticated(authResult)

    res
      .recover {
        case _: InvalidPasswordException  => InvalidPassword
        case _: IdentityNotFoundException => UserNotFound
      }

  }
}

/**
  * Handles actions to users.
  *
  * @param accountRepo The Account repository.
  * @param silhouette
  * @param passwordHasherRegistry The password hasher registry.
  * @param accountActivationRepo       The auth token repository implementation.
  * @param credentialsProvider
  * @param clock                  The clock instance.
  * @param rememberMeConfig
  */
@Singleton
class AccountService @Inject()(accountRepo: AccountRepository,
                               silhouette: Silhouette[CookieEnv],
                               passwordHasherRegistry: PasswordHasherRegistry,
                               accountActivationRepo: AccountActivationRepository,
                               credentialsProvider: CredentialsProvider,
                               clock: Clock,
                               rememberMeConfig: RememberMeConfig) {
  import AccountService._

  def signIn(email: String, password: String, rememberMe: Boolean, response: Result)(
      implicit ec: ExecutionContext,
      request: Request[AnyContent]
  ): Future[SignInStatus] = {
    val credentials = Credentials(email, password)

    val res = for {
      loginInfo <- credentialsProvider.authenticate(credentials)
      retrieved <- accountRepo.retrieve(loginInfo)
      user <- retrieved match {
        case None    => Future.failed(new IdentityNotFoundException("Account not found!"))
        case Some(v) => Future.successful(v)
      }
      cookieAuth <- silhouette.env.authenticatorService
        .create(loginInfo)
        .map {
          case authenticator if rememberMe =>
            authenticator.copy(
              expirationDateTime = clock.now + rememberMeConfig.authenticatorExpiry,
              idleTimeout = Some(rememberMeConfig.authenticatorIdleTimeout),
              cookieMaxAge = Some(rememberMeConfig.cookieMaxAge)
            )
          case authenticator => authenticator
        }
      cookie <- silhouette.env.authenticatorService.init(cookieAuth)
      authResult <- silhouette.env.authenticatorService.embed(cookie, response)
    } yield Authenticated(authResult)

    res
      .recover {
        case _: InvalidPasswordException  => InvalidPassword
        case _: IdentityNotFoundException => UserNotFound
      }

  }

  def register(username: UsernameString,
               email: EmailString,
               firstname: String,
               lastname: String,
               password: String)(
      implicit ec: ExecutionContext,
      request: play.api.mvc.RequestHeader
  ): Future[RegistrationStatus] = {

    val loginInfo = LoginInfo(CredentialsProvider.ID, email.value)

    for {
      userRetrieved <- accountRepo
        .retrieve(loginInfo)
      result <- userRetrieved match {
        // account is already registered within the system
        case Some(_) => Future.successful(UserAlreadyExists)

        // save account info
        case None =>
          val passwordInfo = passwordHasherRegistry.current.hash(password)

          for {
            account <- accountRepo.createUser(
              username,
              email,
              firstname,
              lastname,
              passwordInfo,
              loginInfo
            )
            _ <- accountActivationRepo.create(account.id)
            // FIXME: something wrong with event bus???
//                     _ <- accountEventBus.publishSignUpEvent(account, request)
          } yield RegistrationSucceed(account)
      }
    } yield result

  }
}

object AccountService {

  import com.mohiva.play.silhouette.api.services.AuthenticatorResult

  sealed trait RegistrationStatus
  case object UserAlreadyExists extends RegistrationStatus
  final case class RegistrationSucceed(user: Account) extends RegistrationStatus

  sealed trait SignInStatus
  final case class Authenticated(result: AuthenticatorResult) extends SignInStatus
  case object UserNotFound extends SignInStatus
  case object InvalidPassword extends SignInStatus
}
