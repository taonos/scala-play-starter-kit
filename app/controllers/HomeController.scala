package controllers

import DAL.DAO._
import javax.inject._

import play.api.mvc._
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.api.actions.{SecuredRequest, UserAwareRequest}
import org.webjars.play.WebJarsUtil
import scala.concurrent.{ExecutionContext, Future}
import Domain.repository.CookieEnv

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(
    cc: ControllerComponents,
    silhouette: Silhouette[CookieEnv],
    accountRepo: AccountDAO,
    productRepo: ProductDAO
)(implicit ec: ExecutionContext, webJarsUtil: WebJarsUtil, assets: AssetsFinder)
    extends AbstractController(cc)
    with I18nSupport {

  /**
    * Handles the index action.
    *
    * @return The result to display.
    */
  def index = silhouette.UserAwareAction.async {
    implicit request: UserAwareRequest[CookieEnv, AnyContent] =>
      request.identity match {
        case Some(v) => Future.successful(Ok(views.html.signedInHome(v)))
        case None    => Future.successful(Ok(views.html.home()))
      }

  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async {
    implicit request: SecuredRequest[CookieEnv, AnyContent] =>
      val result = Redirect(routes.HomeController.index())
      silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
      silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
