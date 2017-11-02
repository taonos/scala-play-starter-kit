package utility.authentication

import javax.inject.Inject

import com.mohiva.play.silhouette.api.actions.SecuredErrorHandler
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.Results.Redirect
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

/**
  * Custom secured error handler.
  *
  * Make sure to disable the default error handler provided by Silhouette in `application.conf` with the following code
  * `play.modules.disabled += "com.mohiva.play.silhouette.api.actions.SecuredErrorHandlerModule"`
  *
  * @param messagesApi The Play messages API.
  */
final class CustomSecuredErrorHandler @Inject()(val messagesApi: MessagesApi)
    extends SecuredErrorHandler
    with I18nSupport {

  /**
    * Called when a not authenticated user tries to access a secured endpoint.
    *
    * As defined by RFC 2616, the status code of the response should be 401 Unauthorized.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthenticated(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Redirect(controllers.routes.SignInController.view()))
  }

  /**
    * Called when an authenticated but not authorized tries to access a secured endpoint.
    *
    * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    Future.successful(
      Redirect(controllers.routes.SignInController.view())
        .flashing("error" -> Messages("access.denied"))
    )
  }
}
