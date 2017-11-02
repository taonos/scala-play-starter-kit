package forms

import play.api.data.Form
import play.api.data.Forms._
import utility.RefinedTypes._
import eu.timepit.refined.api.RefType.applyRef

/**
  * The form which handles the submission of the credentials.
  */
object SignInForm {

  import ExtendedForms._

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "email" -> email,
      "password" -> password,
      "rememberMe" -> boolean
    )(Data.fromForm)(Data.unapply)
  )

  /**
    * The form data.
    *
    * @param email The email of the user.
    * @param password The password of the user.
    * @param rememberMe Indicates if the user should stay logged in on the next visit.
    */
  final case class Data(email: EmailString, password: PasswordString, rememberMe: Boolean)

  object Data {
    private[SignInForm] def unapply(arg: Data): Option[(String, String, Boolean)] = {
      import eu.timepit.refined.auto._
      Some((arg.email, arg.password, arg.rememberMe))
    }

    private[SignInForm] def fromForm(email: String, password: String, rememberMe: Boolean): Data =
      Data(
        applyRef[EmailString].unsafeFrom(email),
        applyRef[PasswordString].unsafeFrom(password),
        rememberMe
      )
  }
}
