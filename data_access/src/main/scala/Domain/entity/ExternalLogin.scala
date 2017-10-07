//package Domain.entity
//
//import enumeratum.EnumEntry.LowerCamelcase
//import enumeratum._
//
//sealed trait ExternalProvider extends EnumEntry with LowerCamelcase
//
//object ExternalProvider extends Enum[ExternalProvider] {
//  val values = findValues
//
//  case object Credentials extends ExternalProvider
//  case object WeChat extends ExternalProvider
//  case object AliPay extends ExternalProvider
//  case object QQ extends ExternalProvider
//  case object TaoBao extends ExternalProvider
//  case object Weibo extends ExternalProvider
//}
//
//final case class ExternalLogin(provider: ExternalProvider, key: String, belongsTo: UserId)
