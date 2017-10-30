package DAL.table

import java.time.LocalDateTime

final case class CreationTime(value: LocalDateTime = LocalDateTime.now)

object CreationTime {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[CreationTime, LocalDateTime](_.value)

  implicit val decode = MappedEncoding[LocalDateTime, CreationTime](CreationTime.apply)

  object auto {

    @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
    implicit def creationTimeToLocalDateTime(v: CreationTime): LocalDateTime = v.value
  }
}
