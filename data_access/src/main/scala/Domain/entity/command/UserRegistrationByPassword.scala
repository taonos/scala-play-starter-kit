package Domain.entity.command

import java.util.UUID

final case class UserRegistrationByPassword(private[Domain] val id: UUID = UUID.randomUUID(), username: String, email: String, firstname: String, lastname: String, password: String)
