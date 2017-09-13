name := "boilerplate"
 
version := "1.0"
      
lazy val `boilerplate` = (project in file(".")).enablePlugins(PlayScala)
//  .enablePlugins(FlywayPlugin)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.3"

//// Navigate from an error page to the source code
//// Using the play.editor configuration option, you can set up Play to add hyperlinks to an error page.
//// This will link to runtime exceptions thrown when Play is running development mode.
//// You can easily navigate from error pages to IntelliJ directly into the source code, by using IntelliJ’s
//// “remote file” REST API with the built in IntelliJ web server on port 63342.
//fork := true // required for "sbt run" to pick up javaOptions
//
//javaOptions += "-Dplay.editor=http://localhost:63342/api/file/?file=%s&line=%s"

libraryDependencies ++=
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
  Seq(
    "com.typesafe.slick" %% "slick",
    "com.typesafe.slick" %% "slick-hikaricp"
//    "com.typesafe.slick" %% "slick-testkit" % Test
  ).map(_ % "3.2.1") ++
  Seq(
    "com.github.tminglei" %% "slick-pg",
    "com.github.tminglei" %% "slick-pg_jts",
    "com.github.tminglei" %% "slick-pg_circe-json"
  ).map(_ % "0.15.3") ++
//  Seq(
//    "com.liyaos" %% "scala-forklift-slick" % "0.3.1"
//  ) ++
//  Seq("org.postgresql" % "postgresql" % "9.4-1201-jdbc41") ++
  Seq("com.typesafe.play" %% "play-slick" % "3.0.1") ++
//  Seq("org.flywaydb" %% "flyway-play" % "4.0.0") ++
  Seq("org.typelevel" %% "cats-core" % "0.9.0") ++
  Seq("com.chuusai" %% "shapeless" % "2.3.2")

//unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

      