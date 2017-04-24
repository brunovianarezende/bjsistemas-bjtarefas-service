import sbt.Keys.libraryDependencies

val slickVersion = "3.2.0"

val logDependencies = libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3"
)

val akkaHttpVersion = "10.0.5"

val serviceDependencies = libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.7.21" % "test"
)

val apiDependencies = libraryDependencies ++= Seq(
  "org.scalaz" %% "scalaz-core" % "7.2.10",
  "com.google.inject" % "guice" % "4.1.0",
  "mysql" % "mysql-connector-java" % "5.1.23",
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "com.typesafe.slick" %% "slick" % slickVersion,
  "mysql" % "mysql-connector-java" % "5.1.23",
  "commons-codec" % "commons-codec" % "1.9",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "org.mockito" % "mockito-core" % "2.7.21" % "test"
)

val commonSettings = Seq(
  organization := "nom.bruno",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.1",
  resolvers += Classpaths.typesafeReleases
)

lazy val root = (project in file("."))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(addCommandAlias("testAll", ";api/test;apiIntegration/test;service/test"): _*)
  .aggregate(api, service)

lazy val api = (project in file("api"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .settings(apiDependencies)

lazy val apiIntegration = (project in file("apiIntegration"))
  .disablePlugins(RevolverPlugin)
  .settings(commonSettings: _*)
  .dependsOn(api % "compile->compile;test->test")


lazy val service = (project in file("service"))
  .settings(commonSettings: _*)
  .settings(serviceDependencies)
  .settings(logDependencies)
  .dependsOn(api)

onLoad in Global := (onLoad in Global).value andThen (Command.process("project service", _))
