package utility.authentication

import javax.inject.{Inject, Singleton}

import Domain.entity._
import Domain.repository.CookieEnv
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.{LoginInfo, Silhouette}
import com.mohiva.play.silhouette.api.util.Clock
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import play.api.mvc.{Cookie, RequestHeader, Result}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ExecutionContext, Future}

final case class RememberMeConfig(cookieMaxAge: FiniteDuration,
                                  authenticatorIdleTimeout: FiniteDuration,
                                  authenticatorExpiry: FiniteDuration)

/**
  *
  * @param silhouette
  * @param clock                  The clock instance.
  * @param rememberMeConfig
  */
@Singleton
class CookieService @Inject()(silhouette: Silhouette[CookieEnv],
                              credentialsProvider: CredentialsProvider,
                              clock: Clock,
                              rememberMeConfig: RememberMeConfig) {

  /**
    * Generate session token based on login info.
    *
    * @param account  User account.
    * @param rememberMe Whether to extend the expiration time of session.
    * @param ec         Execution context.
    * @param request    The original request carrying the login info.
    * @return
    */
  def embedCookie(
      account: Account,
      rememberMe: Boolean,
      response: Result
  )(implicit ec: ExecutionContext, request: RequestHeader): Future[AuthenticatorResult] = {
    import com.mohiva.play.silhouette.api.Authenticator.Implicits._
    import eu.timepit.refined.auto._

    for {
      // create cookie authenticator
      cookieAuthenticator <- silhouette.env.authenticatorService
        .create(LoginInfo(credentialsProvider.id, account.email))
        .map {
          // if user elects to have a longer lasting session
          case authenticator if rememberMe =>
            authenticator.copy(
              expirationDateTime = clock.now + rememberMeConfig.authenticatorExpiry,
              idleTimeout = Some(rememberMeConfig.authenticatorIdleTimeout),
              cookieMaxAge = Some(rememberMeConfig.cookieMaxAge)
            )
          case authenticator => authenticator
        }
      // create cookie
      cookie <- silhouette.env.authenticatorService.init(cookieAuthenticator)
      authResult <- silhouette.env.authenticatorService.embed(cookie, response)
    } yield authResult
  }
}
