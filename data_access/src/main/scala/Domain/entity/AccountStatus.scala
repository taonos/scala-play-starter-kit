package Domain.entity

import enumeratum._

sealed trait AccountStatus extends EnumEntry

object AccountStatus extends Enum[AccountStatus] {
  val values = findValues

  case object AlreadyExists extends AccountStatus
  case object NotRegistered extends AccountStatus
  final case class Registered(user: Account) extends AccountStatus
}
