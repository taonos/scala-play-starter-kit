package controllers

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api._
import org.webjars.play.WebJarsUtil
import play.api.i18n.I18nSupport
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import Domain.service.AccountService
import Domain.service.AccountService.{RegistrationSucceed, UserAlreadyExists}
import forms.SignUpForm
import scala.concurrent.{ExecutionContext, Future}

/**
  * The `Sign Up` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param accountService         The user service implementation.
  * @param ec                     The execution context.
  * @param webJarsUtil            The webjar util.
  * @param assets                 The Play assets finder.
  */
@Singleton
class SignUpController @Inject()(
    components: ControllerComponents,
    silhouette: Silhouette[Domain.repository.DefaultEnv],
    accountService: AccountService
)(
    implicit ec: ExecutionContext,
    webJarsUtil: WebJarsUtil,
    assets: AssetsFinder
) extends AbstractController(components)
    with I18nSupport {

  /**
    * Views the `Sign Up` page.
    *
    * @return The result to display.
    */
  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.signUp(SignUpForm.form)))
  }

  /**
    * Handles the submitted form.
    *
    * @return The result to display.
    */
  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        for {
          registration <- accountService.register(
                           data.firstName + " " + data.lastName,
                           data.email,
                           data.firstName,
                           data.lastName,
                           data.password
                         )
          res <- registration match {
                  case UserAlreadyExists =>
                    Future.successful(
                      Redirect(routes.SignUpController.view())
                        .flashing(
                          "warning" -> "Account already exists. Please choose a different name."
                        )
                    )
                  case RegistrationSucceed(_) =>
                    Future.successful(
                      Redirect(routes.SignInController.view())
                        .flashing("info" -> "Sign up successful! Please sign in!")
                    )
                }
        } yield res

      }
    )
  }
}
