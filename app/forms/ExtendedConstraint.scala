package forms

import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

/**
  * @define passwordDoc Defines an 'password' constraint for `String` values which will verify the complexity of the password.
  */
trait ExtendedConstraints {

  import utility.Regex._

  def password(errorMessage: String = "error.password"): Constraint[String] =
    Constraint[String]("constraint.password") { e =>
      passwordRegex.r
        .findFirstMatchIn(e)
        .map(_ => Valid)
        .getOrElse(Invalid(ValidationError(errorMessage)))
    }

  def password: Constraint[String] = password()

  def username(errorMessage: String = "error.username"): Constraint[String] =
    Constraint[String]("constraint.username") { e =>
      usernameRegex.r
        .findFirstMatchIn(e)
        .map(_ => Valid)
        .getOrElse(Invalid(ValidationError(errorMessage)))
    }

  def username: Constraint[String] = username()
}

object ExtendedConstraints extends ExtendedConstraints
