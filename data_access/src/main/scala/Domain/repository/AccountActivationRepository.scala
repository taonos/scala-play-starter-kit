package Domain.repository

import java.util.UUID
import javax.inject.{Inject, Singleton}

import DAL.DAO.AccountActivationTokenDAO
import DAL.DbContext
import DAL.table.{AccountActivationTokenId, AccountActivationTokenTable, AccountId}
import Domain.entity
import Domain.entity.AccountActivationToken
import com.mohiva.play.silhouette.api.util.Clock
import org.joda.time.DateTimeZone
import shapeless.tag

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountActivationRepository @Inject()(val ctx: DbContext, clock: Clock)
    extends AccountActivationTokenDAO {

  import scala.concurrent.duration._
  import mapping.implicits._

  /**
    * Creates a new auth token and saves it in the backing store.
    *
    * @param userID The user ID for which the token should be created.
    * @param expiry The duration a token expires.
    * @return The saved auth token.
    */
  def create(userID: UUID, expiry: FiniteDuration = 5.minutes)(
      implicit ec: ExecutionContext
  ): Future[AccountActivationTokenTable] = {
    val token = AccountActivationToken(
      tag[entity.AccountActivationTokenId][UUID](UUID.randomUUID()),
      userID,
      clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt)
    )
    val res = AccountActivationTokenDAO.insert(authTokenToAuthTable(token))

    ctx.performIO(res)
  }

  /**
    * Validates a token ID.
    *
    * @param id The token ID to validate.
    * @return The token if it's valid, None otherwise.
    */
  def validate(
      id: UUID
  )(implicit ec: ExecutionContext): Future[Option[AccountActivationTokenTable]] =
    ctx.performIO(AccountActivationTokenDAO.findByPk(DAL.table.AccountActivationTokenId(id: UUID)))

}
