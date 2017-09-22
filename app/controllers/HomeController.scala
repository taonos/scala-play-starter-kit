package controllers

import DAL.DAO._
import javax.inject._
import play.api.mvc._
import monix.execution.Scheduler.Implicits.global

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject() (cc: ControllerComponents, accountRepo: AccountDAO, productRepo: ProductDAO)
    extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/`.
    */
//  def index = Action {
//    Ok(views.html.index("Your new application is ready."))
//  }

  def index = Action.async { implicit request =>
    val s = accountRepo.all zip productRepo.all
    s.map { r =>
      Ok(views.html.demo(r._1, r._2))
    }.runAsync
  }
}