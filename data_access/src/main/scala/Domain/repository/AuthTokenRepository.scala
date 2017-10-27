package Domain.repository

import java.util.UUID
import javax.inject.{Inject, Singleton}
import DAL.DAO.AuthTokenDAO
import DAL.table.{AccountId, AuthTokenId, AuthTokenTable}
import Domain.entity
import Domain.entity.AuthToken
import com.mohiva.play.silhouette.api.util.Clock
import org.joda.time.DateTimeZone
import shapeless.tag
import scala.concurrent.Future

@Singleton
class AuthTokenRepository @Inject()(authTokenDAO: AuthTokenDAO, clock: Clock) {

  import AuthTokenRepository._
  import scala.concurrent.duration._

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param userID The user ID for which the token should be created.
    * @param expiry The duration a token expires.
    * @return The saved auth token.
    */
  def create(userID: UUID, expiry: FiniteDuration = 5.minutes): Future[AuthTokenTable] = {
    val token = AuthToken(
      tag[entity.AuthTokenId][UUID](UUID.randomUUID()),
      userID,
      clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt)
    )
    authTokenDAO.insert(authTokenToAuthTable(token))
  }

  /**
    * Validates a token ID.
    *
    * @param id The token ID to validate.
    * @return The token if it's valid, None otherwise.
    */
  def validate(id: UUID) = authTokenDAO.findByPk(DAL.table.AuthTokenId(id: UUID))

}

object AuthTokenRepository {
  private def authTokenToAuthTable(v: AuthToken) = {
    AuthTokenTable(AuthTokenId(v.id: UUID), AccountId(v.userID: UUID), v.expiry)
  }
}
