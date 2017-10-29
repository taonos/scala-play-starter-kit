import sbt._

trait Entirety {
  val toSeq: Seq[ModuleID]
}

object Dependencies {
  private object Version {
    val specs2 = "3.6.6"
    val silhouette = "5.0.0"
    val akka = "2.4.18"
    val circe = "0.8.0"
    val monix = "2.3.0"
  }

  object Library {

    object Play extends Entirety {
      val mailer: ModuleID = "com.typesafe.play" %% "play-mailer" % "6.0.1"
      val mailerGuice: ModuleID = "com.typesafe.play" %% "play-mailer-guice" % "6.0.1"

      override val toSeq: Seq[ModuleID] = Seq(mailer, mailerGuice)
    }

    object Silhouette extends Entirety {
      val core: ModuleID = "com.mohiva" %% "play-silhouette" % Version.silhouette

      val passwordBcrypt
        : ModuleID = "com.mohiva" %% "play-silhouette-password-bcrypt" % Version.silhouette
      val persistence: ModuleID = "com.mohiva" %% "play-silhouette-persistence" % Version.silhouette
      val cryptoJca: ModuleID = "com.mohiva" %% "play-silhouette-crypto-jca" % Version.silhouette

      val testkit
        : ModuleID = "com.mohiva" %% "play-silhouette-testkit" % Version.silhouette % "test"

      override val toSeq: Seq[ModuleID] = Seq(
        core,
        passwordBcrypt,
        persistence,
        cryptoJca,
        testkit
      )
    }

    object Circe extends Entirety {
      val core: ModuleID = "io.circe" %% "circe-core" % Version.circe
      val generic: ModuleID = "io.circe" %% "circe-generic" % Version.circe
      val parser: ModuleID = "io.circe" %% "circe-parser" % Version.circe
      val optics: ModuleID = "io.circe" %% "circe-optics" % Version.circe

      override val toSeq: Seq[ModuleID] = Seq(
        core,
        generic,
        parser,
        optics
      )
    }

    object Enumeratum extends Entirety {
      val core: ModuleID = "com.beachape" %% "enumeratum" % "1.5.12"
      val circe: ModuleID = "com.beachape" %% "enumeratum-circe" % "1.5.14"

      override val toSeq: Seq[ModuleID] = Seq(
        core,
        circe
      )
    }

    object Shapeless {
      val core: ModuleID = "com.chuusai" %% "shapeless" % "2.3.2"
    }

    object Cats {
      val core: ModuleID = "org.typelevel" %% "cats-core" % "0.9.0"
    }

    object FlywayDB {
      val core: ModuleID = "org.flywaydb" % "flyway-core" % "4.2.0"
      val play: ModuleID = "org.flywaydb" %% "flyway-play" % "4.0.0"
    }

    object Monix extends Entirety {
      val eval: ModuleID = "io.monix" %% "monix-eval" % Version.monix
      val cats: ModuleID = "io.monix" %% "monix-cats" % Version.monix

      override val toSeq: Seq[ModuleID] = Seq(eval, cats)
    }

    object JavaxInject {
      val inject: ModuleID = "javax.inject" % "javax.inject" % "1"
    }

    object PostgreSQL {
      val db: ModuleID = "org.postgresql" % "postgresql" % "42.1.4"
    }

    object Quill {
      val asyncpostgresql: ModuleID = "io.getquill" %% "quill-async-postgres" % "1.4.0"
    }

    object WebJars extends Entirety {

      val play: ModuleID = "org.webjars" %% "webjars-play" % "2.6.1"

      val bootstrap
        : ModuleID = "org.webjars" % "bootstrap" % "3.3.7-1" exclude ("org.webjars", "jquery")
      val jquery: ModuleID = "org.webjars" % "jquery" % "3.2.1"

      override val toSeq: Seq[ModuleID] = Seq(play, bootstrap, jquery)
    }

    object Bootstrap {

      val core: ModuleID = "com.adrianhurt" %% "play-bootstrap" % "1.2-P26-B3"
    }

    object PureConfig {
      val core: ModuleID = "com.github.pureconfig" %% "pureconfig" % "0.8.0"
    }

    object JodaTime {
      val core: ModuleID = "joda-time" % "joda-time" % "2.9.9"
    }

    object Refined extends Entirety {
      val core: ModuleID = "eu.timepit" %% "refined" % "0.8.4"

      val pureconfig
        : ModuleID = "eu.timepit" %% "refined-pureconfig" % "0.8.4" // optional, JVM-only

      override val toSeq: Seq[ModuleID] = Seq(core, pureconfig)
    }
  }
}
