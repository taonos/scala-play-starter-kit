package controllers

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
//import models.User
//import models.services.{AuthTokenService, UserService}
//import org.webjars.play.WebJarsUtil
//import play.api.i18n.{I18nSupport, Messages}
//import play.api.libs.mailer.{Email, MailerClient}
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import Domain.entity._
import Domain.service.UserService
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

/**
  * The default env.
  */
trait TestEnv extends Env {
  type I = User
  type A = CookieAuthenticator
}
import Domain.service.AuthTokenService

/**
  * The `Sign Up` controller.
  *
  * @param components             The Play controller components.
  * @param silhouette             The Silhouette stack.
  * @param userService            The user service implementation.
  * @param authInfoRepository     The auth info repository implementation.
  * @param authTokenService       The auth token service implementation.
//  * @param avatarService          The avatar service implementation.
  * @param passwordHasherRegistry The password hasher registry.
//  * @param mailerClient           The mailer client.
//  * @param webJarsUtil            The webjar util.
  * @param assets                 The Play assets finder.
  */
@Singleton
class SignUpController @Inject()(
    components: ControllerComponents,
    silhouette: Silhouette[TestEnv],
    userService: UserService,
    authInfoRepository: AuthInfoRepository,
    authTokenService: AuthTokenService,
    passwordHasherRegistry: PasswordHasherRegistry
    //                                   mailerClient: MailerClient
)(
    implicit
//                                   webJarsUtil: WebJarsUtil,
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
        val result = Redirect(routes.SignUpController.view())
          .flashing("info" -> Messages("sign.up.email.sent", data.email))
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)

        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
//            val url = routes.SignInController.view().absoluteURL()

            Future.successful(result)
          case None =>
            val passwordInfo = passwordHasherRegistry.current.hash(data.password)

            for {
              // TODO: implement username from form
              user <- userService.createUser(data.firstName + " " + data.lastName,
                                             data.email,
                                             data.firstName,
                                             data.lastName,
                                             passwordInfo,
                                             loginInfo)
              authInfo  <- authInfoRepository.add(loginInfo, passwordInfo)
              authToken <- authTokenService.create(user.id).runAsync
            } yield {
//              val url = routes.ActivateAccountController.activate(authToken.id).absoluteURL()

              silhouette.env.eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}
