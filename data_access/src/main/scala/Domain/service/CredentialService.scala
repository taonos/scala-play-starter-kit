package Domain.service

import javax.inject.{Inject, Singleton}

import Domain.repository.CredentialRepository
import Domain.entity._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CredentialService @Inject()(credentialRepo: CredentialRepository)(
    implicit ec: ExecutionContext
) {

  def hasCredential(account: Account): Future[Boolean] =
    credentialRepo.find(account).map(_.fold(false)(_ => true))
}
