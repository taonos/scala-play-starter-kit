name := "boilerplate"

addCommandAlias("mgm", "migration_manager/run")

addCommandAlias("mg", "migrations/run")

lazy val commonSettings = Seq(
  version := "1.0",
  scalaVersion := "2.12.3",
  scalacOptions += "-deprecation",
  scalacOptions += "-feature",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
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
//  "com.h2database" % "h2" % "1.4.192",
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
  //  Seq(
  //    "com.liyaos" %% "scala-forklift-slick" % "0.3.1"
  //  ) ++
  //  Seq("org.postgresql" % "postgresql" % "9.4-1201-jdbc41") ++
  Seq("com.typesafe.play" %% "play-slick" % "3.0.1") ++
  //  Seq("org.flywaydb" %% "flyway-play" % "4.0.0") ++
  Seq("org.typelevel" %% "cats-core" % "0.9.0") ++
  Seq("com.chuusai" %% "shapeless" % "2.3.2")

lazy val migrationsDependencies =
  dbDeps ++ forkliftDeps ++ loggingDeps

lazy val migrationManagerDependencies = dbDeps ++ forkliftDeps

lazy val generatedCodeDependencies = slickDeps




lazy val `boilerplate` = (project in file("."))
  .dependsOn(generatedCode)
  .settings(commonSettings:_*)
  .settings {
    libraryDependencies ++= appDependencies
  }
  .enablePlugins(PlayScala)

lazy val migrationManager = project.in(file("migration_manager"))
  .settings(commonSettings:_*)
  .settings {
    libraryDependencies ++= migrationManagerDependencies
  }

lazy val migrations = project.in(file("migrations"))
  .dependsOn(generatedCode, migrationManager)
  .settings(commonSettings:_*)
  .settings {
    libraryDependencies ++= migrationsDependencies
  }

lazy val tools = Project("git-tools", file("tools/git"))
  .settings(commonSettings:_*)
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

      