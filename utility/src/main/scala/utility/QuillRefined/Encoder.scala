package utility.QuillRefined

import io.getquill.context.async.{AsyncContext, SqlTypes}
import utility.RefinedTypes._

trait Encoder { this: AsyncContext[_, _, _] =>

  implicit val nonEmptyStringEncoder: Encoder[NonEmptyString] =
    encoder[NonEmptyString](SqlTypes.VARCHAR)

  implicit val usernameStringEncoder: Encoder[UsernameString] =
    encoder[UsernameString](SqlTypes.VARCHAR)

  implicit val emailStringEncoder: Encoder[EmailString] =
    encoder[EmailString](SqlTypes.VARCHAR)
}
