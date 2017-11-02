package Domain.entity

trait SignInStatus
sealed trait SignInError extends SignInStatus
sealed trait SignInResult extends SignInStatus

object SignInStatus {

  final case class Success(account: Account) extends SignInResult
  case object UserNotFound extends SignInError
  case object InvalidPassword extends SignInError
}
