package controllers

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api._
import forms.SignInForm
import Domain.service._
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import scala.concurrent.{ExecutionContext, Future}
import Domain.repository.DefaultEnv
import AccountService._

/**
  * The `Sign In` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param accountService         The user service implementation.
  * @param ec                     The execution context.
  * @param webJarsUtil            The webjar util.
  * @param assets                 The Play assets finder.
  */
@Singleton
class SignInController @Inject()(
    components: ControllerComponents,
    silhouette: Silhouette[DefaultEnv],
    accountService: AccountService,
)(
    implicit ec: ExecutionContext,
    webJarsUtil: WebJarsUtil,
    assets: AssetsFinder
) extends AbstractController(components)
    with I18nSupport {

  /**
    * Views the `Sign In` page.
    *
    * @return The result to display.
    */
  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signIn(SignInForm.form)))
  }

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        val result = Redirect(routes.HomeController.index())
        accountService
          .signIn(data.email, data.password, data.rememberMe, result)
          .map {
            case Authenticated(r) => r
            case UserNotFound =>
              Redirect(routes.SignInController.view())
                .flashing("error" -> "User not found")
            case InvalidPassword =>
              Redirect(routes.SignInController.view())
                .flashing("error" -> "Invalid password")
          }
      }
    )
  }
}
