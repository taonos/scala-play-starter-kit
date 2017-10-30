package utility.QuillRefined

import eu.timepit.refined.api.RefType
import io.getquill.context.async.{AsyncContext, SqlTypes}
import utility.RefinedTypes._

trait Decoder { this: AsyncContext[_, _, _] =>

  implicit val nonEmptyStringDecoder: Decoder[NonEmptyString] =
    decoder[NonEmptyString]({
      case v: String =>
        RefType.applyRef[NonEmptyString].unsafeFrom(v)
    }, SqlTypes.VARCHAR)

  implicit val usernameStringDecoder: Decoder[UsernameString] =
    decoder[UsernameString]({
      case v: String =>
        RefType.applyRef[UsernameString].unsafeFrom(v)
    }, SqlTypes.VARCHAR)

  implicit val emailStringDecoder: Decoder[EmailString] =
    decoder[EmailString]({
      case v: String => RefType.applyRef[EmailString].unsafeFrom(v)
    }, SqlTypes.VARCHAR)
}
