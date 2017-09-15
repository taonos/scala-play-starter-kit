package controllers

import javax.inject._

import play.api.mvc._
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.jdbc.PostgresProfile.api._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import datamodel.latest.schema.Tables._
import slick.basic.DatabaseConfig

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               dbConfigProvider: DatabaseConfigProvider)
    extends AbstractController(cc) {

  val dbConfig: DatabaseConfig[JdbcProfile] = dbConfigProvider.get[JdbcProfile]

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
    val resultingUsers: Future[Seq[UsersRow]] = dbConfig.db.run(Users.result)
    val resultingProducts: Future[Seq[ProductsRow]] = dbConfig.db.run(Products.result)
    val combined = resultingUsers zip resultingProducts
    combined.map { case (users, products) => Ok(views.html.demo(users, products)) }
  }
}
