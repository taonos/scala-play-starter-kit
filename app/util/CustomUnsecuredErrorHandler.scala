package util

import com.mohiva.play.silhouette.api.actions.UnsecuredErrorHandler
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results.Redirect

import scala.concurrent.Future

/**
  * Custom unsecured error handler.
  *
  * Make sure to disable the default error handler provided by Silhouette in `application.conf` with the following code
  * `play.modules.disabled += "com.mohiva.play.silhouette.api.actions.UnsecuredErrorHandlerModule"`
  *
  */
class CustomUnsecuredErrorHandler extends UnsecuredErrorHandler {

  /**
    * Called when a user is authenticated but not authorized.
    *
    * As defined by RFC 2616, the status code of the response should be 403 Forbidden.
    *
    * @param request The request header.
    * @return The result to send to the client.
    */
  override def onNotAuthorized(implicit request: RequestHeader): Future[Result] = {
    Future.successful(Redirect(controllers.routes.HomeController.index()))
  }
}
