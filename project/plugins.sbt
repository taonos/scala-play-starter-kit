logLevel := Level.Warn

resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")

//addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.2.0")

addSbtPlugin("org.danielnixon" % "sbt-playwarts" % "1.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.6.0")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-RC11")

// if you use coursier, you must use sbt-scalafmt-coursier
addSbtPlugin("com.lucidchart" % "sbt-scalafmt-coursier" % "1.12")

// project/plugins.sbt
libraryDependencies += "com.geirsson" %% "scalafmt-bootstrap" % "0.6.6"

// A scala compiler plugin for more concise errors
// This plugin removes some of the redundancy of the compiler output and prints additional info for implicit resolution
// errors.
//addCompilerPlugin("io.tryp" %% "splain" % "0.2.5")
