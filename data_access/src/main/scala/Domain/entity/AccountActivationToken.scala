package Domain.entity

import java.util.UUID
import org.joda.time.DateTime
import shapeless.tag.@@

sealed trait AccountActivationTokenId

/**
  * A token to authenticate a user against an endpoint for a short time period.
  *
  * @param id The unique token ID.
  * @param userID The unique ID of the user the token is associated with.
  * @param expiry The date-time the token expires.
  */
final case class AccountActivationToken(id: UUID @@ AccountActivationTokenId,
                                        userID: UUID,
                                        expiry: DateTime)
