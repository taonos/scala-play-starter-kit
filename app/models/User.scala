package models

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

import scala.concurrent.ExecutionContext.Implicits.global
import db.DbProvider

import scala.concurrent.Future



final case class User(id: Int = 0, created_at: LocalDateTime, updated_at: LocalDateTime, firstname: String, lastname: String)

@Singleton
class Users @Inject() (val dbProvider: DbProvider) {
  import dbProvider.ctx._

  val users = quote(querySchema[User]("user"))

  def find(id: Int): Future[Option[User]] =
    run(users
      .filter(user => user.id == lift(id))
    )
      .map(_.headOption)

  def all: Future[List[User]] = run(users)
}

final case class Product(id: Int = 0, created_at: LocalDateTime, updated_at: LocalDateTime, name: String)

@Singleton
class Products @Inject() (val dbProvider: DbProvider) {
  import dbProvider.ctx._

  val products = quote(querySchema[Product]("product"))

  def find(id: Int): Future[Option[Product]] =
    run(products
      .filter(product => product.id == lift(id))
    )
      .map(_.headOption)

  def all: Future[List[Product]] = run(products)
}

//case class Ownership()