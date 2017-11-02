package Domain.entity.command

import utility.RefinedTypes.{EmailString, PasswordString}

final case class SignInByPassword(email: EmailString, password: PasswordString)
