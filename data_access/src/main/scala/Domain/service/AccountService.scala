package Domain.service

import javax.inject.{Inject, Singleton}

import Domain.entity.User
import Domain.repository.{AccountEventBus, AccountRepository, AuthTokenRepository, DefaultEnv}
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.{
  IdentityNotFoundException,
  InvalidPasswordException
}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.mvc.{AnyContent, Request, Result}

import scala.concurrent.Future

/**
  * Handles actions to users.
  *
  * @param accountRepo The User repository.
  * @param silhouette
  * @param passwordHasherRegistry The password hasher registry.
  * @param authInfoRepo     The auth info repository implementation.
  * @param authTokenRepo       The auth token repository implementation.
  * @param credentialsProvider
  * @param clock                  The clock instance.
  * @param accountEventBus
  * @param rememberMeConfig
  */
@Singleton
class AccountService @Inject()(accountRepo: AccountRepository,
                               silhouette: Silhouette[DefaultEnv],
                               passwordHasherRegistry: PasswordHasherRegistry,
                               authInfoRepo: AuthInfoRepository,
                               authTokenRepo: AuthTokenRepository,
                               credentialsProvider: CredentialsProvider,
                               clock: Clock,
                               accountEventBus: AccountEventBus,
                               rememberMeConfig: RememberMeConfig) {

  import monix.execution.Scheduler.Implicits.global
  import AccountService._

  def signIn(email: String, password: String, rememberMe: Boolean, response: Result)(
      implicit request: Request[AnyContent]): Future[SignInStatus] = {
    val credentials = Credentials(email, password)

    val res = for {
      loginInfo <- credentialsProvider.authenticate(credentials)
      user <- accountRepo.retrieve(loginInfo).flatMap {
               case None    => Future.failed(new IdentityNotFoundException("User not found!"))
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
      _ <- accountEventBus.publishSignInEvent(user, request)
      cookie <- silhouette.env.authenticatorService.init(cookieAuth)
      authResult <- silhouette.env.authenticatorService.embed(cookie, response)
    } yield Authenticated(authResult)

    res
      .recover {
        case _: InvalidPasswordException  => InvalidPassword
        case _: IdentityNotFoundException => UserNotExist
      }

//    credentialsProvider
//      .authenticate(credentials)
//      .flatMap { loginInfo => accountRepo.retrieve(loginInfo).map((loginInfo, _)) }
//      .flatMap {
//        case (loginInfo, Some(user)) =>
//          // Creates a new Authenticator from the an identity's login information. This action should be executed after
//          // a successful authentication. The created Authenticator can then be used to recognize the user on every
//          // subsequent request.
//          silhouette.env.authenticatorService.create(loginInfo)
//            .map {
//              case authenticator if rememberMe =>
//
//                authenticator.copy(
//                  expirationDateTime = clock.now + rememberMeConfig.authenticatorExpiry,
//                  idleTimeout = Some(rememberMeConfig.authenticatorIdleTimeout),
//                  cookieMaxAge = Some(rememberMeConfig.cookieMaxAge)
//                )
//              case authenticator => authenticator
//            }
//            .flatMap { authenticator =>
//              accountEventBus.publishSignInEvent(user, request)
//              silhouette.env.authenticatorService.init(authenticator).flatMap { v =>
//                silhouette.env.authenticatorService.embed(v, result)
//              }
//            }
//
//
//        case (_, None) => Future.successful(UserNotExist)
//      }
//      .recover {
//        case _: InvalidPasswordException => InvalidPassword
//        case _:
//      }
  }

  def register(username: String,
               email: String,
               firstname: String,
               lastname: String,
               password: String)(
      implicit request: play.api.mvc.RequestHeader): Future[Either[RegistrationStatus, User]] = {
    val loginInfo = LoginInfo(CredentialsProvider.ID, email)

    //        val e: Future[Either[RegistrationStatus, User]] = accountRepo
    //          .retrieve(loginInfo)
    //          .flatMap { userRetrieved =>
    //
    //            userRetrieved match {
    //              case None => ???
    //              case Some(_) => Left(UserAlreadyExists)
    //            }
    //
    //            ???
    //          }

    for {
      userRetrieved <- accountRepo
                        .retrieve(loginInfo)
      result <- userRetrieved match {
                 // account is already registered within the system
                 case Some(_) => Future.successful(Left(UserAlreadyExists))

                 // save account info
                 case None =>
                   val passwordInfo = passwordHasherRegistry.current.hash(password)

                   for {
                     account <- accountRepo.createUser(username,
                                                       email,
                                                       firstname,
                                                       lastname,
                                                       passwordInfo,
                                                       loginInfo)
                     authInfo <- authInfoRepo.add(loginInfo, passwordInfo)
                     authToken <- authTokenRepo.create(account.id).runAsync
                     _ <- accountEventBus.publishSignUpEvent(account, request)
                   } yield Right(account)
               }
    } yield result

  }
}

object AccountService {

  import com.mohiva.play.silhouette.api.services.AuthenticatorResult

  sealed trait RegistrationStatus
  case object UserAlreadyExists extends RegistrationStatus

  sealed trait SignInStatus
  final case class Authenticated(result: AuthenticatorResult) extends SignInStatus
  case object UserNotExist extends SignInStatus
  case object InvalidPassword extends SignInStatus
}
