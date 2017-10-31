package modules

import com.google.inject.name.Named
import com.google.inject.{AbstractModule, Provides}
import utility.ExecutionContextFactory
import java.time.Clock
import services._
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
class BaseModule extends AbstractModule {

  override def configure() = {

    // Use the system clock as the default implementation of Clock
    bind(classOf[Clock]).toInstance(Clock.systemDefaultZone)
    // Ask Guice to create an instance of ApplicationTimer when the
    // application starts.
    bind(classOf[ApplicationTimer]).asEagerSingleton()

    ()
  }

  @Provides
  @Named("cpu-execution-context")
  def provideCPUExecutionContext: ExecutionContext = ExecutionContextFactory.cpuExecutionContext
//  @Provides
//  @Named("i/o execution context")
//  def provideIOExecutionContext: ExecutionContext =
//    ExecutionContextFactory.ioExecutionContext

}

object BaseModule {}
