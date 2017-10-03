package DAL

import javax.inject.{Inject, Singleton}
import io.getquill.context.async.{SqlTypes, TransactionalExecutionContext, AsyncContext}
import io.getquill.{ImplicitQuery, PostgresAsyncContext, PostgresEscape, SnakeCase}
import monix.eval.Task
import org.joda.time.{DateTime => JodaDateTime}

@Singleton
class DbContext @Inject()()
    extends PostgresAsyncContext[PostgresEscape with SnakeCase]("db.default")
    with ImplicitQuery
    with Encoder
    with Decoder {

  def transaction_task[T](f: TransactionalExecutionContext => Task[T]): Task[T] =
    Task.deferFutureAction { implicit scheduler =>
      transaction { a: TransactionalExecutionContext =>
        f(a).runAsync
      }
    }
}

//TODO: decoder and encoder for joda-time will be obsolete when quill 1.4.1 is released.

trait Decoder { this: AsyncContext[_, _, _] =>

  implicit val jodaDateTimeDecoder: Decoder[JodaDateTime] = decoder[JodaDateTime]({
    case dateTime: JodaDateTime => dateTime
  }, SqlTypes.TIMESTAMP)

}

trait Encoder { this: AsyncContext[_, _, _] =>

  implicit val jodaDateTimeEncoder: Encoder[JodaDateTime] =
    encoder[JodaDateTime](SqlTypes.TIMESTAMP)
}
