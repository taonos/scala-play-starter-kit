name := "boilerplate"

// Add any command aliases that may be useful as shortcuts
addCommandAlias("mgm", "migration_manager/run")

addCommandAlias("mg", "migrations/run")

addCommandAlias("cc", ";clean;compile")

// workaround for scalafmt in sbt 0.13
def latestScalafmt = "1.2.0"
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

lazy val playWartRemoverSettings = Seq(
  wartremoverWarnings ++= Seq(
//    PlayWart.AssetsObject,
    PlayWart.CookiesPartial,
    PlayWart.FlashPartial,
    PlayWart.FormPartial,
    PlayWart.HeadersPartial,
    PlayWart.JavaApi,
    PlayWart.JsLookupResultPartial,
    PlayWart.JsReadablePartial,
    PlayWart.LangObject,
//    PlayWart.MessagesObject,
    PlayWart.SessionPartial,
    PlayWart.TypedMapPartial,
    PlayWart.WSResponsePartial
  ),
  wartremoverExcluded += baseDirectory.value / "conf" / "routes"
)

lazy val wartremoverSettings = Seq(

  // more at wartremover.org/doc/warts.html
  wartremoverWarnings ++= Seq(
    Wart.AsInstanceOf, // type conversion hurts typesafety
    Wart.EitherProjectionPartial, // the 'get' method can throw an exception
    Wart.Enumeration, // Scala's enumerations are slow, use ADTs
    Wart.ExplicitImplicitTypes, // implicits must have explicit type annotations
    Wart.FinalCaseClass, // case class must be sealed - they meant to be simple data types
    Wart.FinalVal, // final vals cause inconsistency during incremental compilation
    Wart.ImplicitConversion, // implicit conversions are dangerous
    Wart.IsInstanceOf, // pattern matching is safer
    Wart.JavaConversions, // use java collections explicitly
    Wart.LeakingSealed, // the subclasses of sealed traits must be final to avoid leaking
    Wart.MutableDataStructures, // mutable data structures in Scala are generally useless
    Wart.Null, // null is unsafe and useless in Scala
    Wart.OptionPartial, // don't use Option's get method, it might throw exceptions
    Wart.Return, // return is spaghetti(and breaks referential transparency)
    Wart.StringPlusAny, // concatenate only a String with an other String
    Wart.Throw, // don't throw exceptions, use Either or Option
    Wart.TraversableOps, // get, head, tail etc. are unsafe - possible exceptions
    Wart.TryPartial, // Try's get is unsafe
    Wart.Var,
    Wart.While // these are only useful at micro-optimizations, use tail recursion instead
  )
//  wartremoverExcluded += baseDirectory.value / "conf" / "routes"
)

lazy val forkliftVersion = "0.3.1"
lazy val slickVersion = "3.2.1"

lazy val loggingDeps = List(
  "org.slf4j" % "slf4j-nop" % "1.6.4" // <- disables logging
)


lazy val slickDeps = Seq(
    "com.typesafe.slick" %% "slick" % slickVersion
    //    "com.typesafe.slick" %% "slick-testkit" % Test
  )

lazy val dbDeps = Seq(
  "org.postgresql" % "postgresql" % "42.1.4",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  ) ++
  Seq(
    "com.github.tminglei" %% "slick-pg",
    "com.github.tminglei" %% "slick-pg_jts",
    "com.github.tminglei" %% "slick-pg_circe-json"
  ).map(_ % "0.15.3")

lazy val forkliftDeps = Seq(
  "com.liyaos" %% "scala-forklift-slick" % forkliftVersion,
  "io.github.nafg" %% "slick-migration-api" % "0.4.1"
)

lazy val appDependencies = dbDeps ++ loggingDeps ++
  Seq( jdbc , ehcache , ws , specs2 % Test , guice ) ++
  Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser",
    "io.circe" %% "circe-optics"
  ).map(_ % "0.8.0") ++
  //  Seq("play-circe" %% "play-circe" % "2608.4") ++
  Seq(
    "com.beachape" %% "enumeratum" % "1.5.12",
    "com.beachape" %% "enumeratum-circe" % "1.5.14"
  ) ++
  Seq("com.typesafe.play" %% "play-slick" % "3.0.1") ++
  Seq("org.typelevel" %% "cats-core" % "0.9.0") ++
  Seq("com.chuusai" %% "shapeless" % "2.3.2")

lazy val migrationsDependencies =
  dbDeps ++ forkliftDeps ++ loggingDeps

lazy val migrationManagerDependencies = dbDeps ++ forkliftDeps

lazy val generatedCodeDependencies = slickDeps




lazy val `boilerplate` = (project in file("."))
  .dependsOn(generatedCode)
  .settings(commonSettings:_*)
  .settings(playWartRemoverSettings:_*)
  .settings {
    libraryDependencies ++= appDependencies
  }
  .enablePlugins(PlayScala)
  .enablePlugins(DockerPlugin)

lazy val migrationManager = project.in(file("migration_manager"))
  .settings(commonSettings:_*)
  .settings(wartremoverSettings:_*)
  .settings {
    libraryDependencies ++= migrationManagerDependencies
  }

lazy val migrations = project.in(file("migrations"))
  .dependsOn(generatedCode, migrationManager)
  .settings(commonSettings:_*)
  .settings(wartremoverSettings:_*)
  .settings {
    libraryDependencies ++= migrationsDependencies
  }

lazy val tools = Project("git-tools", file("tools/git"))
  .settings(commonSettings:_*)
  .settings(wartremoverSettings:_*)
  .settings {
    libraryDependencies ++= forkliftDeps ++ List(
      "com.liyaos" %% "scala-forklift-git-tools" % forkliftVersion,
      "com.typesafe" % "config" % "1.3.0",
      "org.eclipse.jgit" % "org.eclipse.jgit" % "4.0.1.201506240215-r"
    )
  }

lazy val generatedCode = project.in(file("generated_code"))
  .settings(commonSettings:_*)
  .settings {
    libraryDependencies ++= generatedCodeDependencies
  }


//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

      