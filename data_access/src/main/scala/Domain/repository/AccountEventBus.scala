package Domain.repository

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api._
import play.api.mvc.{AnyContent, Request}

@Singleton
class AccountEventBus @Inject()(silhouette: Silhouette[CookieEnv]) {

  private val publish = silhouette.env.eventBus.publish _

  def publishSignUpEvent[I <: Identity](identity: I, request: play.api.mvc.RequestHeader): Unit = {
    publish(SignUpEvent(identity, request))
  }

  def publishSignInEvent[I <: Identity](identity: I, request: Request[AnyContent]): Unit = {
    publish(LoginEvent(identity, request))
  }

  def publishSignOutEvent[I <: Identity](identity: I, request: Request[AnyContent]): Unit = {
    publish(LogoutEvent(identity, request))
  }

  def publishAuthenticatedEvent[I <: Identity](identity: I, request: Request[AnyContent]): Unit = {
    publish(AuthenticatedEvent(identity, request))
  }

  def publishNotAuthenticatedEvent(request: Request[AnyContent]): Unit = {
    publish(NotAuthenticatedEvent(request))
  }

  def publishNotAuthorizedEvent[I <: Identity](identity: I, request: Request[AnyContent]): Unit = {
    publish(NotAuthorizedEvent(identity, request))
  }
}
