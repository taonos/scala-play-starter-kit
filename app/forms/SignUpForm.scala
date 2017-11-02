package forms

import eu.timepit.refined.api.RefType.applyRef
import play.api.data.Form
import play.api.data.Forms._
import utility.RefinedTypes._

/**
  * The form which handles the sign up process.
  */
object SignUpForm {

  import ExtendedForms._

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "username" -> username,
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password" -> password
    )(SignUpData.fromForm)(SignUpData.unapply)
  )

  /**
    * The form data.
    *
    * @param username The user name of a user.
    * @param firstName The first name of a user.
    * @param lastName The last name of a user.
    * @param email The email of the user.
    * @param password The password of the user.
    */
  final case class SignUpData(username: UsernameString,
                              firstName: NonEmptyString,
                              lastName: NonEmptyString,
                              email: EmailString,
                              password: PasswordString)

  object SignUpData {

    private[SignUpForm] def unapply(
        arg: SignUpData
    ): Option[(String, String, String, String, String)] = {
      import eu.timepit.refined.auto._
      Some(
        (
          /**
            * With the implicits imported via `eu.timepit.refined.auto._`, refined types are implicitly converted to
            * the corresponding unrefined types.
            */
          arg.username,
          arg.firstName,
          arg.lastName,
          arg.email,
          arg.password
        )
      )
    }

    private[SignUpForm] def fromForm(username: String,
                                     firstName: String,
                                     lastName: String,
                                     email: String,
                                     password: String): SignUpData = {
      SignUpData(
        // Since the data is already verified by form, we can unsafely convert unrefined types to refined.
        applyRef[UsernameString].unsafeFrom(username),
        applyRef[NonEmptyString].unsafeFrom(firstName),
        applyRef[NonEmptyString].unsafeFrom(lastName),
        applyRef[EmailString].unsafeFrom(email),
        applyRef[PasswordString].unsafeFrom(password)
      )
    }
  }
}
