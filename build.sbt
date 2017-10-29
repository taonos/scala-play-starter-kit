import Dependencies.Library._

name := "scala-play-starter-kit"

// Add any command aliases that may be useful as shortcuts
addCommandAlias("cc", ";clean;compile")
addCommandAlias("populate", "population/run")
addCommandAlias("db_migrate", "migration/flywayMigrate")
addCommandAlias("db_clean", "migration/flywayClean")
addCommandAlias("cd", "project")
addCommandAlias("ls", "projects")
addCommandAlias("cr", ";clean ;reload")
addCommandAlias("cru", ";clean ;reload ;test:update")
addCommandAlias("du", "dependencyUpdates")
addCommandAlias("rdu", ";reload ;dependencyUpdates")
addCommandAlias("ru", ";reload ;test:update")
addCommandAlias("tc", "test:compile")

// workaround for scalafmt in sbt 0.13
lazy val latestScalafmt = "1.2.0"
commands += Command.args("scalafmt", "Run scalafmt cli.") {
  case (state, args) =>
    val Right(scalafmt) =
      org.scalafmt.bootstrap.ScalafmtBootstrap.fromVersion(latestScalafmt)
    scalafmt.main("--non-interactive" +: args.toArray)
    state
}

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.12.4",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  resolvers += "micronautics/scala on bintray" at "http://dl.bintray.com/micronautics/scala",
  scalacOptions ++= Seq(
//    "-P:splain:implicits:false",
    "-deprecation", // warn about deprecated code
    "-encoding",
    "UTF-8", // UTF-8 should be the default file encoding everywhere
    "-explaintypes", // explain type errors with more details
    "-feature", // should enable Scala features explicitly
    "-language:higherKinds", // higher-kinded types are useful with typeclasses
    "-language:implicitConversions", // implicit parameters and classes are useful
    "-unchecked", // static code shouldn't depend on assumptions
    //  "-Xfatal-warnings",             // warnings SHOULD be errors - it will help with code smells
    "-Xfuture", // enable future language features
    "-Xlint:delayedinit-select", // selecting member of DelayedInit
    "-Xlint:doc-detached", // warn when a scaladoc is detached from its element
    "-Xlint:infer-any", // Any shouldn't be infered - it's unsafe
    "-Xlint:missing-interpolator", // a variable isn't defined in a string interpolation
    "-Xlint:nullary-override", // warn when 'def f()' overrides 'def f'
    "-Xlint:nullary-unit", // warn when nullary methods return with Unit
    "-Xlint:private-shadow", // something shadows a private member
    "-Xlint:stars-align", // pattern sequence wildcard must align with sequence component
    "-Xlint:type-parameter-shadow", // something local shadows a type parameter
    "-Xlint:unsound-match", // the used pattern matching is unsafe
    "-Ywarn-dead-code", // warn about unused code
    "-Ywarn-extra-implicit", // there should be a max of 1 implicit parameter for each definition
    "-Ywarn-inaccessible", // warn about inaccessible types in method signatures
//    "-Ywarn-unused:imports", // warn about unused imports
//    "-Ywarn-unused:locals", // warn about unused local variables
//    "-Ywarn-unused:params", // warn about unused parameters
//    "-Ywarn-unused:patvars", // warn about unused pattern matching variables
//    "-Ywarn-unused:privates", // warn about unused private members
    "-Ywarn-value-discard", // warn when a non-Unit expression is unused
    "-Ypartial-unification" //if running 2.12
  ),
  // Note that the REPL can?t really cope with -Ywarn-unused:imports or -Xfatal-warnings so you should turn them off for
  // the console.
  scalacOptions in (Compile, console) ~= (_.filterNot(
    Set(
      "-Ywarn-unused:imports",
      "-Xfatal-warnings"
    )
  ))
)

lazy val commonWartRemoverSettings = Seq(
  // more at wartremover.org/doc/warts.html
  wartremoverWarnings in (Compile, compile) ++= Seq(
    Wart.ArrayEquals,
//    Wart.Any,
    Wart.AnyVal,
    Wart.AsInstanceOf, // type conversion hurts typesafety
    Wart.EitherProjectionPartial, // the 'get' method can throw an exception
    Wart.Enumeration, // Scala's enumerations are slow, use ADTs
    Wart.ExplicitImplicitTypes, // implicits must have explicit type annotations
    Wart.FinalCaseClass, // case class must be sealed - they meant to be simple data types
    Wart.FinalVal, // final vals cause inconsistency during incremental compilation
    Wart.ImplicitConversion, // implicit conversions are dangerous
    Wart.IsInstanceOf, // pattern matching is safer
    Wart.LeakingSealed, // the subclasses of sealed traits must be final to avoid leaking
    Wart.Null, // null is unsafe and useless in Scala
    Wart.OptionPartial, // don't use Option's get method, it might throw exceptions
    Wart.Return, // return is spaghetti(and breaks referential transparency)
    Wart.StringPlusAny, // concatenate only a String with an other String
    Wart.Throw, // don't throw exceptions, use Either or Option
    Wart.TraversableOps, // get, head, tail etc. are unsafe - possible exceptions
    Wart.TryPartial, // Try's get is unsafe
    Wart.Var,
    Wart.While // these are only useful at micro-optimizations, use tail recursion instead
    // the following options somehow triggers a `NullPointerException` with quill in use during compilation...
//    Wart.JavaConversions, // use java collections explicitly
//    Wart.MutableDataStructures, // mutable data structures in Scala are generally useless
  )
)

lazy val rootWartRemoverSettings = commonWartRemoverSettings ++
  Seq(
    // Exlucde play routes from wartremover
    wartremoverExcluded ++= routes.in(Compile).value
  )

lazy val populationWartRemoverSettings = commonWartRemoverSettings
lazy val dataAccessWartRemoverSettings = commonWartRemoverSettings

lazy val populationDependencies = Seq(PostgreSQL.db, Quill.asyncpostgresql) ++ Monix.toSeq

lazy val dataAccessDependencies = Seq(
  PostgreSQL.db,
  Quill.asyncpostgresql,
  JavaxInject.inject,
  Shapeless.core,
  JodaTime.core,
  Enumeratum.core,
  Refined.core
) ++ Monix.toSeq ++ Silhouette.toSeq

lazy val migrationDependencies = Seq(PostgreSQL.db, FlywayDB.core)

lazy val utilityDependencies = Seq(
  Refined.core
)

lazy val appDependencies = Seq(
  jdbc,
  ehcache,
  ws,
  specs2 % Test,
  guice,
  PostgreSQL.db,
  Quill.asyncpostgresql,
  FlywayDB.play,
  Cats.core,
  Shapeless.core,
  Bootstrap.core,
  PureConfig.core,
  Enumeratum.core,
  Refined.core
) ++
  Monix.toSeq ++
  Silhouette.toSeq ++
  Circe.toSeq ++
  Enumeratum.toSeq ++
  WebJars.toSeq

import org.flywaydb.sbt.FlywayPlugin._
lazy val conf =
  com.typesafe.config.ConfigFactory
    .parseFile(new File("configuration/src/main/resources/db.conf"))
    .resolve()
lazy val flywaySettings = Seq(
  flywayUrl := conf.getString("db.default.url"),
  flywayUser := conf.getString("db.default.user"),
  flywayPassword := conf.getString("db.default.password"),
  flywayLocations := Seq("filesystem:conf/db/migration"),
  flywayValidateOnMigrate := true
//  flywayLocations := Seq("classpath:db/migration")
)

lazy val utility = (project in file("utility"))
  .settings(commonSettings: _*)
  .settings(commonWartRemoverSettings: _*)
  .settings {
    libraryDependencies ++= utilityDependencies
  }

lazy val `scala-play-starter-kit` = (project in file("."))
  .dependsOn(utility, `data_access`, configuration)
  .aggregate(utility, `data_access`, configuration)
  .settings(commonSettings: _*)
  .settings(rootWartRemoverSettings: _*)
  .settings {
    libraryDependencies ++= appDependencies
  }
  .enablePlugins(PlayScala)
  .enablePlugins(DockerPlugin)

lazy val configuration = (project in file("configuration"))
  .settings(commonSettings: _*)

lazy val migration = (project in file("migration"))
  .dependsOn(configuration)
  .settings(commonSettings: _*)
  .settings(commonWartRemoverSettings: _*)
  .settings(flywaySettings: _*)
  .settings {
    libraryDependencies ++= migrationDependencies
  }
  .enablePlugins(FlywayPlugin)

lazy val population = (project in file("population"))
  .dependsOn(`data_access`, configuration)
  .aggregate(`data_access`, configuration)
  .settings(commonSettings: _*)
  .settings(populationWartRemoverSettings: _*)
  .settings {
    libraryDependencies ++= populationDependencies
  }

lazy val `data_access` = (project in file("data_access"))
  .dependsOn(utility)
  .aggregate(utility)
  .settings(commonSettings: _*)
  .settings(dataAccessWartRemoverSettings: _*)
  .settings {
    libraryDependencies ++= dataAccessDependencies
  }

//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )
