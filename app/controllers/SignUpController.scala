package controllers

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers._
import org.webjars.play.WebJarsUtil
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import Domain.entity._
import Domain.service.AccountService
import forms.SignUpForm
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

/**
  * The default env.
  */

import Domain.service.AuthTokenService

/**
  * The `Sign Up` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param accountService            The user service implementation.
  * @param webJarsUtil            The webjar util.
  * @param assets                 The Play assets finder.
  */
@Singleton
class SignUpController @Inject()(
                                  components: ControllerComponents,
                                  silhouette: Silhouette[Domain.repository.TestEnv],
                                  accountService: AccountService
)(
    implicit
    webJarsUtil: WebJarsUtil,
    assets: AssetsFinder
) extends AbstractController(components)
    with I18nSupport {

  import monix.execution.Scheduler.Implicits.global

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
          r <- accountService.register(data.firstName + " " + data.lastName,
            data.email,
            data.firstName,
            data.lastName,
            data.password)
          x <- r match {
            case Left(_) => Future.successful(Redirect(routes.SignUpController.view()).flashing("warning" -> "User already exists. Please choose a different name."))
            case Right(v) => Future.successful(Redirect(routes.SignUpController.view())
              .flashing("info" -> "Sign up successful!"))
          }
        } yield x

      }
    )
  }
}
