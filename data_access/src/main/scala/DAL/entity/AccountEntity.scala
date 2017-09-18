package DAL.entity

import java.time.LocalDateTime
import java.time.LocalDateTime.now

final case class AccountEntity(firstname: String, lastname: String, id: Int = 0, created_at: LocalDateTime = now, updated_at: LocalDateTime = now)
