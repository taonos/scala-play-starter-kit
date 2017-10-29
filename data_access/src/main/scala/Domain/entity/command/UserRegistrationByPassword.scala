package Domain.entity.command

import java.util.UUID

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.NonEmpty
import utility.RefinedTypes.UsernameString

final case class UserRegistrationByPassword(private[Domain] val id: UUID = UUID.randomUUID(),
                                            username: UsernameString,
                                            email: String,
                                            firstname: String,
                                            lastname: String,
                                            password: String)
