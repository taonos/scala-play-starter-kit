package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api._
import forms.SignInForm
import Domain.service._
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc._
import utility.authentication.CookieService

import scala.concurrent.{ExecutionContext, Future}
import Domain.repository.CookieEnv

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
    silhouette: Silhouette[CookieEnv],
    accountService: AccountService,
    cookieService: CookieService
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
  def view: Action[AnyContent] = silhouette.UnsecuredAction.async { implicit req =>
    Future.successful(Ok(views.html.signIn(SignInForm.form)))
  }

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit: Action[AnyContent] = {
    import Domain.entity.SignInStatus._
    silhouette.UnsecuredAction.async { implicit request =>
      SignInForm.form.bindFromRequest.fold(
        form => Future.successful(BadRequest(views.html.signIn(form))),
        data => {
          accountService
            .signIn(data.email, data.password)
            .flatMap {
              case Success(acc) =>
                cookieService
                  .embedCookie(acc, data.rememberMe, Redirect(routes.HomeController.index()))
              case UserNotFound =>
                Future.successful(
                  Redirect(routes.SignInController.view())
                    .flashing("error" -> "Account not found")
                )
              case InvalidPassword =>
                Future.successful(
                  Redirect(routes.SignInController.view())
                    .flashing("error" -> "Invalid password")
                )
            }
        }
      )
    }
  }
}
