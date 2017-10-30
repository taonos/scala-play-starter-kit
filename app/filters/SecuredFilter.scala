package filters

import javax.inject.{Inject, Singleton}

import Domain.repository.CookieEnv
import akka.stream.Materializer
import com.mohiva.play.silhouette.api.Silhouette
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SecuredFilter @Inject()(silhouette: Silhouette[CookieEnv], bodyParsers: PlayBodyParsers)(
    implicit override val mat: Materializer,
    exec: ExecutionContext
) extends Filter {

  private lazy val redirectHome: Future[Result] =
    Future.successful(Results.Redirect(controllers.routes.HomeController.index()))

  override def apply(f: RequestHeader => Future[Result])(rh: RequestHeader): Future[Result] = {
    val action = silhouette.UserAwareAction.async(bodyParsers.empty) { r =>
      rh.path match {
        case "/signin" if r.identity.isDefined => redirectHome
        case "/signup" if r.identity.isDefined => redirectHome
        case _                                 => f(rh)
      }
    }

    action(rh).run
  }
}
