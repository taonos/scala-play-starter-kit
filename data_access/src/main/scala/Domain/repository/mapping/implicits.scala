package Domain.repository.mapping

import java.util.UUID

import Domain.entity._
import DAL.table._
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import shapeless.tag

private[repository] object implicits {
  import eu.timepit.refined.auto._

  @throws[IllegalArgumentException]
  implicit def passwordInfoToCredentialTable(v: PasswordInfo): CredentialTable = {
    CredentialTable(Hasher.withName(v.hasher), HashedPassword.unsafeFrom(v.password), v.salt)
  }

  implicit def credentialTableToPasswordInfo(v: CredentialTable): PasswordInfo = {
    PasswordInfo(v.hasher.entryName, v.hashedPassword.value, v.salt)
  }

  @throws[IllegalArgumentException]
  implicit def accountTableToUser(v: AccountTable, l: LoginInfo): Account =
    Account(
      v.id.value,
      v.username.value,
      v.email.value,
      LoginProvider.unsafeFrom(l.providerID, l.providerKey)
    )

  @throws[IllegalArgumentException]
  implicit def accountCredentialTableToUser(v: AccountCredentialTable, l: LoginInfo): Account =
    Account(
      v.accountId.value,
      v.username.value,
      v.email.value,
      LoginProvider.unsafeFrom(l.providerID, l.providerKey)
    )

  @throws[IllegalArgumentException]
  implicit def loginInfoToAccountEmail(v: LoginInfo): AccountEmail =
    AccountEmail.unsafeFrom(v.providerKey)

  implicit def authTokenToAuthTable(v: AccountActivationToken) = {
    AccountActivationTokenTable(
      AccountActivationTokenId(v.id: UUID),
      AccountId(v.userID: UUID),
      v.expiry
    )
  }

  implicit def AuthTableToAuthToken(v: AccountActivationTokenTable) =
    AccountActivationToken(
      tag[Domain.entity.AccountActivationTokenId][UUID](v.id.value),
      v.accountId.value,
      v.expiry
    )
}
