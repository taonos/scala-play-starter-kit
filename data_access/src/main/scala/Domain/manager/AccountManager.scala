package Domain.manager

import javax.inject.{Inject, Singleton}
import Domain.entity._
import Domain.entity.command._
import Domain.repository.AccountRepository
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.exceptions.InvalidPasswordException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider

import scala.concurrent.{ExecutionContext, Future}

/**
  * Handles actions to users.
  *
  * @param accountRepo The Account repository.
  * @param passwordHasherRegistry The password hasher registry.
  * @param credentialsProvider
  */
@Singleton
class AccountManager @Inject()(accountRepo: AccountRepository,
                               passwordHasherRegistry: PasswordHasherRegistry,
                               credentialsProvider: CredentialsProvider) {
  import eu.timepit.refined.auto._

  def signUp(
      command: SignUpByPassword
  )(implicit ec: ExecutionContext): Future[SignUpStatus] = {
    import SignUpStatus._

    val loginInfo = LoginInfo(credentialsProvider.id, command.email)

    for {
      userRetrieved <- accountRepo.retrieve(loginInfo)
//      a = userRetrieved.fold[AccountStatus](NotRegistered)(_ => AlreadyExists)
      result <- userRetrieved match {
        // save account info
        case None =>
          val passwordInfo = passwordHasherRegistry.current.hash(command.password)

          for {
            account <- accountRepo.createUser(
              command.username,
              command.email,
              command.firstname.value,
              command.lastname.value,
              passwordInfo,
              loginInfo
            )
          } yield Success(account)

        // account is already registered within the system
        case Some(_) => Future.successful(UserAlreadyExists)
      }
    } yield result
  }

  def signIn(command: SignInByPassword)(
      implicit ec: ExecutionContext
  ): Future[SignInStatus] = {
    import SignInStatus._
    val credentials = Credentials(command.email, command.password)

    val res = for {
      // verify credential is correct
      loginInfo <- credentialsProvider.authenticate(credentials)
      // retrieve account based on verified login info
      acc <- accountRepo.retrieve(loginInfo)
    } yield acc

    res
      .map {
        case None    => UserNotFound
        case Some(v) => Success(v)
      }
      .recover {
        case _: InvalidPasswordException => InvalidPassword
      }
  }
}
