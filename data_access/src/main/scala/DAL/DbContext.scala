package DAL

import javax.inject.{Inject, Singleton}
import io.getquill.context.async.TransactionalExecutionContext
import io.getquill.{ImplicitQuery, PostgresAsyncContext, PostgresEscape, SnakeCase}
import monix.eval.Task

@Singleton
class DbContext @Inject() () extends PostgresAsyncContext[PostgresEscape with SnakeCase]("db.default") with ImplicitQuery {

  def transaction_task[T](f: TransactionalExecutionContext => Task[T]): Task[T] =
    Task.deferFutureAction { implicit scheduler =>
      transaction { a: TransactionalExecutionContext =>
        f(a).runAsync
      }
    }
}

object DbContext {

}
