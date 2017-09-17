package db

import javax.inject.Singleton

import io.getquill.{Escape, PostgresAsyncContext, SnakeCase}


@Singleton
class DbProvider {
  val ctx = new PostgresAsyncContext[Escape with SnakeCase]("db.default")
}
