package DAL.table

import java.util.UUID

import enumeratum._
import DAL.DAO.{PK, TableWithPK}
import enumeratum.EnumEntry.Lowercase
import eu.timepit.refined.api.RefType
import utility.RefinedTypes.NonEmptyString

final case class CredentialId(value: UUID = UUID.randomUUID()) extends PK

object CredentialId {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[CredentialId, UUID](_.value)
  implicit val decode = MappedEncoding[UUID, CredentialId](CredentialId.apply)
}

final case class HashedPassword(value: NonEmptyString)

object HashedPassword {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[HashedPassword, NonEmptyString](_.value)
  implicit val decode = MappedEncoding[NonEmptyString, HashedPassword](HashedPassword.apply)

  def unsafeFrom(v: String): HashedPassword =
    HashedPassword(RefType.applyRef[NonEmptyString].unsafeFrom(v))
}

sealed trait Hasher extends EnumEntry

object Hasher extends Enum[Hasher] {

  import io.getquill.MappedEncoding

  implicit val encode = MappedEncoding[Hasher, String](_.entryName)
  implicit val decode = MappedEncoding[String, Hasher](Hasher.withName)

  val values = findValues

  def unsafeFrom(v: String): Hasher = Hasher.withName(v)

  case object `Bcrypt-SHA256` extends Hasher with Lowercase
}

/**
  *
  * @param hasher
  * @param hashedPassword
  * @param salt
  * @param id A unique identifier.
  */
final case class CredentialTable(hasher: Hasher,
                                 hashedPassword: HashedPassword,
                                 salt: Option[String],
                                 id: CredentialId = new CredentialId)
    extends TableWithPK[CredentialId]
    with Timestamped {
  val pk: CredentialId = id
}
