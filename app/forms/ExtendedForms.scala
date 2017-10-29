package forms

import play.api.data.Forms.text
import play.api.data.Mapping

object ExtendedForms {

  val password: Mapping[String] = text verifying ExtendedConstraints.password
  val username: Mapping[String] = text verifying ExtendedConstraints.username
}
