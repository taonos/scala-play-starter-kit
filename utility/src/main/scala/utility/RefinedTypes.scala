package utility

import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection._
import eu.timepit.refined.string.MatchesRegex
import Regex._

object RefinedTypes {

  type NonEmptyString = String Refined NonEmpty
  type EmailString = String Refined MatchesRegex[W.`emailRegex`.T]
  type PasswordString = String Refined MatchesRegex[W.`passwordRegex`.T]
  type UsernameString = String Refined MatchesRegex[W.`usernameRegex`.T]
}
