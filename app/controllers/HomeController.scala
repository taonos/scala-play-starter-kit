package controllers

import DAL.DAO._
import javax.inject._

import play.api.mvc._
import play.api.i18n.I18nSupport
import com.mohiva.play.silhouette.api.{LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.api.actions.SecuredRequest
import monix.execution.Scheduler.Implicits.global
import org.webjars.play.WebJarsUtil
import scala.concurrent.Future
import Domain.repository.TestEnv

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               silhouette: Silhouette[TestEnv],
                               accountRepo: AccountDAO,
                               productRepo: ProductDAO)(implicit
                                                        webJarsUtil: WebJarsUtil,
                                                        assets: AssetsFinder)
    extends AbstractController(cc) with I18nSupport {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
//  def index = Action {
//    Ok(views.html.index("Your new application is ready."))
//  }

//  def index = Action.async { implicit request =>
//    val s = accountRepo.findAll zip productRepo.findAll
//    s.map { r =>
//      Ok(views.html.main(r._1, r._2))
//    }.runAsync
//  }

  /**
    * Handles the index action.
    *
    * @return The result to display.
    */
  def index = silhouette.SecuredAction.async { implicit request: SecuredRequest[TestEnv, AnyContent] =>
    Future.successful(Ok(views.html.home(request.identity)))
  }

  /**
    * Handles the Sign Out action.
    *
    * @return The result to display.
    */
  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[TestEnv, AnyContent] =>
    val result = Redirect(routes.HomeController.index())
    silhouette.env.eventBus.publish(LogoutEvent(request.identity, request))
    silhouette.env.authenticatorService.discard(request.authenticator, result)
  }
}
