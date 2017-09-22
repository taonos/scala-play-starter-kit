package DAL.table

//import model.persistence.{Id => Identifier, HasId}
//import model.persistence.Types.{IdOptionLong, OptionLong}


final case class AccountUsername(value: String) extends AnyVal
final case class AccountTable(username: AccountUsername, email: String, firstname: String, lastname: String) extends Timestamped


//final case class Test(override val id: IdOptionLong = Identifier.empty) extends HasId[Test, OptionLong] with Timestamped
