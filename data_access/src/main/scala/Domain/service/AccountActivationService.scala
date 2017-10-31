package Domain.service

import java.util.UUID
import javax.inject.{Inject, Singleton}

import Domain.repository.AccountActivationRepository

/**
  * Handles actions to auth tokens.
  *
  */
@Singleton
class AccountActivationService @Inject()(
    authTokenRepo: AccountActivationRepository
) {

  import scala.concurrent.duration._

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param id The user ID for which the token should be created.
    * @param expiry The duration a token expires.
    * @return The saved auth token.
    */
  def create(id: UUID, expiry: FiniteDuration = 5.minutes) =
    authTokenRepo.create(id, expiry)

  /**
    * Validates a token ID.
    *
    * @param id The token ID to validate.
    * @return The token if it's valid, None otherwise.
    */
  def validate(id: UUID) = authTokenRepo.validate(id)
}
