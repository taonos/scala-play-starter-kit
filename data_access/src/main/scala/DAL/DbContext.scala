package DAL

import javax.inject.{Singleton, Inject}

import io.getquill.{Literal, PostgresAsyncContext}

@Singleton
class DbContext @Inject() () extends PostgresAsyncContext[Literal]("db.default")
