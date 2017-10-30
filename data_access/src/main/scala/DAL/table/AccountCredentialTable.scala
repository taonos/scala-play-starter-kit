package DAL.table

final case class AccountCredentialTable(accountId: AccountId,
                                        username: AccountUsername,
                                        email: AccountEmail,
                                        credentialId: Option[CredentialId],
                                        hasher: Option[Hasher],
                                        hashedPassword: Option[HashedPassword],
                                        salt: Option[String],
                                        createdAt: CreationTime,
                                        updatedAt: LastUpdateTime)
