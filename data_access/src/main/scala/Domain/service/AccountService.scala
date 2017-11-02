package Domain.service

import javax.inject.{Inject, Singleton}

import Domain.entity.command.{SignInByPassword, SignUpByPassword}
import Domain.manager._
import utility.RefinedTypes.{EmailString, NonEmptyString, PasswordString, UsernameString}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AccountService @Inject()(accountManager: AccountManager) {
  import Domain.entity._

  def signIn(email: EmailString, password: PasswordString)(
      implicit ec: ExecutionContext
  ): Future[SignInStatus] = {
    accountManager
      .signIn(SignInByPassword(email, password))
  }

  def signUp(username: UsernameString,
             email: EmailString,
             firstname: NonEmptyString,
             lastname: NonEmptyString,
             password: PasswordString)(
      implicit ec: ExecutionContext
  ): Future[SignUpStatus] = {

    accountManager.signUp(
      SignUpByPassword(username, email, firstname, lastname, password)
    )
  }
}

object AccountService {}
