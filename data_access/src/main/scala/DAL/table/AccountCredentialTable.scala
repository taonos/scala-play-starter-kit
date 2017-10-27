package DAL.table

import java.time.LocalDateTime
import java.time.LocalDateTime.now

final case class AccountCredentialTable(accountId: AccountId,
                                        username: AccountUsername,
                                        email: String,
                                        credentialId: Option[CredentialId],
                                        hasher: Option[String],
                                        hashedPassword: Option[String],
                                        salt: Option[String],
                                        createdAt: LocalDateTime = now,
                                        updatedAt: LocalDateTime = now)
