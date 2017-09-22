package DAL

import javax.inject.{Inject, Singleton}
import io.getquill.{PostgresAsyncContext, SnakeCase}

@Singleton
class DbContext @Inject() () extends PostgresAsyncContext[SnakeCase]("db.default")
