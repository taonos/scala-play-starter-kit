package Domain.entity

import java.util.UUID

final case class UserInfo(id: UUID,
                          username: String,
                          email: String,
                          firstname: String,
                          lastname: String)
