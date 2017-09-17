package controllers

import javax.inject._

import models.Products
import models.Users

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc._

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cc: ControllerComponents, users: Users, products: Products)
    extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
//  def index = Action {
////    println(test.haha.dbProvider.ctx)
//    Ok(views.html.index("Your new application is ready."))
//  }

  def index = Action.async { implicit request =>
    val s = users.all zip products.all
    s.map { r =>
      Ok(views.html.demo(r._1, r._2))
    }
  }
}