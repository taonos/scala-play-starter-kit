package utility.authentication

import Domain.entity.Account
import com.mohiva.play.silhouette.api.{Authenticator, Authorization}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.mvc.Request

import scala.concurrent.Future

sealed trait AllowedProvider[A <: Authenticator] extends Authorization[Account, A]

object AllowedProvider {

  /**
    * Grants only access if a user has authenticated with the given provider.
    *
    */
  final case class CredentialProvider() extends AllowedProvider[CookieAuthenticator] {

    /**
      * Indicates if a user is authorized to access an action.
      *
      * @param user The usr object.
      * @param authenticator The authenticator instance.
      * @param request The current request.
      * @tparam B The type of the request body.
      * @return True if the user is authorized, false otherwise.
      */
    override def isAuthorized[B](user: Account, authenticator: CookieAuthenticator)(
        implicit request: Request[B]
    ): Future[Boolean] = {

      Future.successful(user.loginProvider.isAuthenticatedViaCredentials)
    }
  }
}
