package controllers

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.Authenticator.Implicits._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Clock, Credentials}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers._
import forms.SignInForm
import Domain.service._
import org.webjars.play.WebJarsUtil
import play.api.Configuration
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import Domain.repository.TestEnv
import AccountService._

/**
  * The `Sign In` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param accountService            The user service implementation.
  * @param webJarsUtil            The webjar util.
  * @param assets                 The Play assets finder.
  */
@Singleton
class SignInController @Inject()(
    components: ControllerComponents,
    silhouette: Silhouette[TestEnv],
    accountService: AccountService,
)(
    implicit
    webJarsUtil: WebJarsUtil,
    assets: AssetsFinder
//                                   ex: ExecutionContext
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
            case UserNotExist => Redirect(routes.SignInController.view())
              .flashing("error" -> "User not found")
            case InvalidPassword => Redirect(routes.SignInController.view())
              .flashing("error" -> "Invalid password")
          }
      }
    )
  }
}
