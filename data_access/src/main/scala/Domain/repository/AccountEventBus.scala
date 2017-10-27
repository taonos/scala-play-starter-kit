package Domain.repository

import javax.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.{Identity, LoginEvent, SignUpEvent, Silhouette}
import play.api.mvc.{AnyContent, Request}
import scala.concurrent.Future

@Singleton
class AccountEventBus @Inject()(silhouette: Silhouette[DefaultEnv]) {

  def publishSignUpEvent[I <: Identity](identity: I,
                                        request: play.api.mvc.RequestHeader): Future[Unit] = {
    Future.successful(silhouette.env.eventBus.publish(SignUpEvent(identity, request)))
  }

  def publishSignInEvent[I <: Identity](identity: I, request: Request[AnyContent]): Future[Unit] = {
    Future.successful(silhouette.env.eventBus.publish(LoginEvent(identity, request)))
  }
}
