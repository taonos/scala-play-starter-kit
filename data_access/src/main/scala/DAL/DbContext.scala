package DAL

import javax.inject.Singleton
import io.getquill._
import utility.QuillRefined

private[DAL] trait Strategy extends SnakeCase

private[DAL] object Strategy extends Strategy

@Singleton
final class DbContext
    extends PostgresAsyncContext[Strategy](Strategy, "db.default")
//    with ImplicitQuery
    with QuillRefined.Decoder
    with QuillRefined.Encoder {}
