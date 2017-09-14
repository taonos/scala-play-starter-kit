logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")

// Database migration
//addSbtPlugin("org.flywaydb" % "flyway-sbt" % "4.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")

// Slick code generation
// https://github.com/tototoshi/sbt-slick-codegen
//addSbtPlugin("com.github.tototoshi" % "sbt-slick-codegen" % "1.2.1")
