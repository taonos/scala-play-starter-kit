package DAL.table

import java.time.LocalDateTime
import java.time.LocalDateTime.now

trait Timestamped {
  val createdAt: LocalDateTime = now
  val updatedAt: LocalDateTime = now
}
