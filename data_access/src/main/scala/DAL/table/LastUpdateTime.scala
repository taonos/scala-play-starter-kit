package DAL.table

import java.time.LocalDateTime

final case class LastUpdateTime(value: LocalDateTime = LocalDateTime.now)

object LastUpdateTime {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[LastUpdateTime, LocalDateTime](_.value)

  implicit val decode = MappedEncoding[LocalDateTime, LastUpdateTime](LastUpdateTime.apply)

  object auto {

    @SuppressWarnings(Array("org.wartremover.warts.ImplicitConversion"))
    implicit def lastUpdateTimeToLocalDateTime(v: LastUpdateTime): LocalDateTime = v.value
  }
}
