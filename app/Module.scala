import DAL.DAO.{AccountCredentialDAO, AccountDAO, CredentialDAO}
import DAL.DbContext
import com.google.inject.{AbstractModule, Provides, TypeLiteral}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{PasswordHasherRegistry, PasswordInfo}
import com.mohiva.play.silhouette.crypto.{
  JcaCrypter,
  JcaCrypterSettings,
  JcaSigner,
  JcaSignerSettings
}
import com.mohiva.play.silhouette.impl.providers.{OAuth1Info, OAuth2Info, OpenIDInfo}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import pureconfig.error.ConfigReaderException
import Domain.service._
import Domain.repository._
import com.google.inject.name.Named
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, FingerprintGenerator, IDGenerator}
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.impl.authenticators.{
  CookieAuthenticator,
  CookieAuthenticatorService,
  CookieAuthenticatorSettings
}
import Domain.service.RememberMeConfig
import play.api.Configuration
import play.api.mvc.CookieHeaderEncoding
import pureconfig._
import utility.ExecutionContextFactory

import scala.concurrent.ExecutionContext

/**
  * This class is a Guice module that tells Guice how to bind several
  * different types. This Guice module is created when the Play
  * application starts.

  * Play will automatically use any class called `Module` that is in
  * the root package. You can create modules in other locations by
  * adding `play.modules.enabled` settings to the `application.conf`
  * configuration file.
  */
class Module extends AbstractModule {
  import Domain.repository.CookieEnv
  import Module._

  override def configure() = {

    // Use the system clock as the default implementation of Clock
//    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
//    bind(classOf[ApplicationTimer]).asEagerSingleton()
    // Set AtomicCounter as the implementation for Counter.
//    bind(classOf[Counter]).to(classOf[AtomicCounter])

    bind(new TypeLiteral[Silhouette[CookieEnv]] {})
      .to(new TypeLiteral[SilhouetteProvider[CookieEnv]] {})
    bind(classOf[FingerprintGenerator]).toInstance(new DefaultFingerprintGenerator(false))
    bind(classOf[com.mohiva.play.silhouette.api.util.Clock])
      .toInstance(com.mohiva.play.silhouette.api.util.Clock())

    bind(classOf[EventBus])

    ()
  }

  @Provides
  @Named("cpu-execution-context")
  def provideCPUExecutionContext: ExecutionContext = ExecutionContextFactory.cpuExecutionContext

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
//                      accountDAO: AccountDAO,
//                      credentialDAO: CredentialDAO,
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

//  @Provides
//  @Named("i/o execution context")
//  def provideIOExecutionContext: ExecutionContext =
//    ExecutionContextFactory.ioExecutionContext

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

object Module {

  /**
    * Provides hint to extract configurations using camel case.
    *
    * @tparam T The type of config.
    * @return A hint for PureConfig.
    */
  implicit def hint[T]: ProductHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
}
