package DAL.entity

import java.time.LocalDateTime
import java.time.LocalDateTime.now

//final case class ProductId(value: Int) extends AnyVal
final case class ProductEntity(name: String, id: Int = 0, created_at: LocalDateTime = now, updated_at: LocalDateTime = now)