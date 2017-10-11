package DAL.table

final case class AccountCredentialTable(accountId: AccountId,
                                        credentialId: Option[CredentialId],
                                        hasher: Option[String],
                                        hashedPassword: Option[String],
                                        salt: Option[String])
    extends Timestamped
