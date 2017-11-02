package Domain.entity

trait SignUpStatus
sealed trait SignUpError extends SignUpStatus
sealed trait SignUpResult extends SignUpStatus

object SignUpStatus {
  case object UserAlreadyExists extends SignUpError
  final case class Success(user: Account) extends SignUpResult
}
