package DAL

import javax.inject.{Inject, Singleton}

import io.getquill.context.async.{AsyncContext, SqlTypes}
import io.getquill.{ImplicitQuery, PostgresAsyncContext, PostgresEscape, SnakeCase}
import org.joda.time.{DateTime => JodaDateTime}
import utility.QuillRefined

@Singleton
class DbContext @Inject()()
    extends PostgresAsyncContext[PostgresEscape with SnakeCase]("db.default")
    with ImplicitQuery
    with Encoder
    with Decoder
    with QuillRefined.Decoder
    with QuillRefined.Encoder {}

// TODO: decoder and encoder for joda-time will be obsolete when quill 1.4.1 is released.

trait Decoder { this: AsyncContext[_, _, _] =>

  implicit val jodaDateTimeDecoder: Decoder[JodaDateTime] = decoder[JodaDateTime]({
    case dateTime: JodaDateTime => dateTime
  }, SqlTypes.TIMESTAMP)
}

trait Encoder { this: AsyncContext[_, _, _] =>

  implicit val jodaDateTimeEncoder: Encoder[JodaDateTime] =
    encoder[JodaDateTime](SqlTypes.TIMESTAMP)
}
