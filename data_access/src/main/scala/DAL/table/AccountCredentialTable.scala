package DAL.table

import java.time.LocalDateTime
import java.time.LocalDateTime.now

final case class AccountCredentialTable(accountId: AccountId,
                                        username: AccountUsername,
                                        email: AccountEmail,
                                        credentialId: Option[CredentialId],
                                        hasher: Option[Hasher],
                                        hashedPassword: Option[HashedPassword],
                                        salt: Option[String],
                                        createdAt: LocalDateTime = now,
                                        updatedAt: LocalDateTime = now)
