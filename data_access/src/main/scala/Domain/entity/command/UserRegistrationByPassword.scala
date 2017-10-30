package Domain.entity.command

import java.util.UUID

import utility.RefinedTypes.{EmailString, NonEmptyString, PasswordString, UsernameString}

final case class UserRegistrationByPassword(private[Domain] val id: UUID = UUID.randomUUID(),
                                            username: UsernameString,
                                            email: EmailString,
                                            firstname: NonEmptyString,
                                            lastname: NonEmptyString,
                                            password: PasswordString)
