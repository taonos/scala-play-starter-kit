package Domain.repository

import Domain.entity.Account
import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

trait DefaultEnv extends Env {
  type I = Account
  type A = CookieAuthenticator
}
