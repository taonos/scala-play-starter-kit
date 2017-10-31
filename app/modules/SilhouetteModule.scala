package modules

import DAL.DAO.AccountCredentialDAO
import DAL.DbContext
import Domain.repository.{AccountRepository, CookieEnv, CredentialRepository}
import Domain.service.RememberMeConfig
import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{
  JcaCrypter,
  JcaCrypterSettings,
  JcaSigner,
  JcaSignerSettings
}
import com.mohiva.play.silhouette.impl.authenticators.{
  CookieAuthenticator,
  CookieAuthenticatorService,
  CookieAuthenticatorSettings
}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import play.api.Configuration
import play.api.i18n.MessagesApi
import play.api.mvc.CookieHeaderEncoding
import pureconfig.error.ConfigReaderException
import pureconfig.{loadConfigOrThrow, CamelCase, ConfigFieldMapping, ProductHint}
import util.{CustomSecuredErrorHandler, CustomUnsecuredErrorHandler}

import scala.concurrent.ExecutionContext

class SilhouetteModule extends AbstractModule {
  import SilhouetteModule._

  override def configure() = {
    bind(new TypeLiteral[Silhouette[CookieEnv]] {})
      .to(new TypeLiteral[SilhouetteProvider[CookieEnv]] {})
    bind(classOf[UnsecuredErrorHandler]).toInstance(new CustomUnsecuredErrorHandler)
    bind(classOf[FingerprintGenerator]).toInstance(new DefaultFingerprintGenerator(false))
    bind(classOf[com.mohiva.play.silhouette.api.util.Clock])
      .toInstance(com.mohiva.play.silhouette.api.util.Clock())

    bind(classOf[EventBus])

    ()
  }

  @Provides
  def provideSecuredErrorHandler(messagesApi: MessagesApi): SecuredErrorHandler =
    new CustomSecuredErrorHandler(messagesApi)

  @Provides
  def provideIDGenerator(
      implicit @Named("cpu-execution-context") ec: ExecutionContext
  ): IDGenerator = {
    new SecureRandomIDGenerator()
  }

  /**
    * Provides the Silhouette environment.
    *
    * @param accountRepo The user service implementation.
    * @param authenticatorService The authentication service implementation.
    * @param eventBus The event bus instance.
    * @return The Silhouette environment.
    */
  @Provides
  def provideEnvironment(
      accountRepo: AccountRepository,
      authenticatorService: AuthenticatorService[CookieAuthenticator],
      eventBus: EventBus
  )(implicit @Named("cpu-execution-context") ec: ExecutionContext): Environment[CookieEnv] = {

    Environment[CookieEnv](
      accountRepo,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  @Provides
  def provideDelegate(
      ctx: DbContext,
      accountCredentialDAO: AccountCredentialDAO
  )(implicit ec: ExecutionContext): DelegableAuthInfoDAO[PasswordInfo] = {
    new CredentialRepository(ctx, accountCredentialDAO)
  }

  //  @Provides
  //  def provideOAuth2InfoDelegableAuthInfoDAO: DelegableAuthInfoDAO[OAuth2Info] = {
  //    new InMemoryAuthInfoDAO[OAuth2Info]
  //  }

  /**
    * Provides the auth info repository.
    *
    * @param credentialRepo The implementation of the delegable password auth info DAO.
    * @return The auth info repository instance.
    */
  @Provides
  def provideAuthInfoRepository(
      credentialRepo: DelegableAuthInfoDAO[PasswordInfo]
  )(implicit @Named("cpu-execution-context") ec: ExecutionContext): AuthInfoRepository = {

    new DelegableAuthInfoRepository(credentialRepo)
  }

  /**
    * Provides the password hasher registry.
    *
    * @return The password hasher registry.
    */
  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  /**
    * Provides the signer for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The signer for the authenticator.
    */
  @Provides
  @Named("authenticator-signer")
  @throws[ConfigReaderException[_]]
  def provideAuthenticatorSigner(configuration: Configuration): Signer = {
    val config = loadConfigOrThrow[JcaSignerSettings]("silhouette.authenticator.signer")

    new JcaSigner(config)
  }

  /**
    * Provides the crypter for the authenticator.
    *
    * @param configuration The Play configuration.
    * @return The crypter for the authenticator.
    */
  @Provides
  @Named("authenticator-crypter")
  @throws[ConfigReaderException[_]]
  def provideAuthenticatorCrypter(configuration: Configuration): Crypter = {
    val config = loadConfigOrThrow[JcaCrypterSettings]("silhouette.authenticator.crypter")

    new JcaCrypter(config)
  }

  /**
    * Provides the authenticator service.
    *
    * @param signer The signer implementation.
    * @param crypter The crypter implementation.
    * @param cookieHeaderEncoding Logic for encoding and decoding `Cookie` and `Set-Cookie` headers.
    * @param fingerprintGenerator The fingerprint generator implementation.
    * @param idGenerator The ID generator implementation.
    * @param configuration The Play configuration.
    * @param clock The clock instance.
    * @return The authenticator service.
    */
  @Provides
  @throws[ConfigReaderException[_]]
  def provideAuthenticatorService(@Named("authenticator-signer") signer: Signer,
                                  @Named("authenticator-crypter") crypter: Crypter,
                                  cookieHeaderEncoding: CookieHeaderEncoding,
                                  fingerprintGenerator: FingerprintGenerator,
                                  idGenerator: IDGenerator,
                                  configuration: Configuration,
                                  clock: Clock)(
      implicit @Named("cpu-execution-context") ec: ExecutionContext
  ): AuthenticatorService[CookieAuthenticator] = {

    val config = loadConfigOrThrow[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(
      config,
      None,
      signer,
      cookieHeaderEncoding,
      authenticatorEncoder,
      fingerprintGenerator,
      idGenerator,
      clock
    )
  }

  @Provides
  @throws[ConfigReaderException[_]]
  def provideRememberMeConfig: RememberMeConfig = {
    loadConfigOrThrow[RememberMeConfig]("silhouette.authenticator.rememberMe")
  }
}

object SilhouetteModule {

  /**
    * Provides hint to extract configurations using camel case.
    *
    * @tparam T The type of config.
    * @return A hint for PureConfig.
    */
  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
}
