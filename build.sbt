name := "scala-play-starter-kit"

// Add any command aliases that may be useful as shortcuts
addCommandAlias("cc", ";clean;compile")
addCommandAlias("populate", "population/run")
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
  scalaVersion := "2.12.3",
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
    "-unchecked",                    // static code shouldn't depend on assumptions
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
    )))
)
lazy val commonWartRemoverSettings = Seq(

  // more at wartremover.org/doc/warts.html
  wartremoverWarnings in (Compile, compile) ++= Seq(
    Wart.ArrayEquals,
    Wart.Any,
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

lazy val forkliftVersion = "0.3.1"
lazy val slickVersion = "3.2.1"

lazy val loggingDeps = Seq(
  "org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
)

lazy val injectDeps = Seq(
  "javax.inject" % "javax.inject" % "1"
)

lazy val quillDeps = Seq(
//  "com.micronautics" %% "has-id" % "1.2.8" withSources(),
  "org.postgresql" % "postgresql" % "42.1.4",
  "io.getquill" %% "quill-async-postgres" % "1.4.0"
)

lazy val monixDeps = Seq(
  "io.monix" %% "monix-eval" % "2.3.0"
)

lazy val populationDependencies = loggingDeps ++ quillDeps ++ monixDeps

lazy val dataAccessDependencies = loggingDeps ++ quillDeps ++ injectDeps ++ monixDeps

lazy val appDependencies = loggingDeps ++ quillDeps ++ monixDeps ++
  Seq( jdbc , ehcache , ws , specs2 % Test , guice ) ++
  Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-optics"
  ).map(_ % "0.8.0") ++
  Seq(
    "com.beachape" %% "enumeratum" % "1.5.12",
    "com.beachape" %% "enumeratum-circe" % "1.5.14"
  ) ++
  Seq("org.flywaydb" %% "flyway-play" % "4.0.0") ++
  Seq("org.typelevel" %% "cats-core" % "0.9.0") ++
  Seq("com.chuusai" %% "shapeless" % "2.3.2")




lazy val `scala-play-starter-kit` = (project in file("."))
  .dependsOn(`data_access`)
  .aggregate(`data_access`)
  .settings(commonSettings:_*)
  .settings(rootWartRemoverSettings:_*)
  .settings {
    libraryDependencies ++= appDependencies
  }
  .enablePlugins(PlayScala)
  .enablePlugins(DockerPlugin)

lazy val population = project.in(file("population"))
  .dependsOn(`data_access`)
  .aggregate(`data_access`)
  .settings(commonSettings:_*)
  .settings(populationWartRemoverSettings:_*)
  .settings {
    libraryDependencies ++= populationDependencies
  }

lazy val `data_access` = project.in(file("data_access"))
  .settings(commonSettings:_*)
  .settings(dataAccessWartRemoverSettings:_*)
  .settings {
    libraryDependencies ++= dataAccessDependencies
  }


//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

      