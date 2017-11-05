import sbt._

object Dependencies {
  lazy val ScalaV = "2.12.3"
  lazy val AkkaHttpV = "10.0.10"
  lazy val AkkaV = "2.5.6"
  lazy val GuiceV = "4.1.0"
  lazy val ScalaTestV = "3.0.4"

  lazy val akkaDeps = Seq(
    "com.typesafe.akka" %% "akka-actor" % AkkaV,
    "com.typesafe.akka" %% "akka-stream" % AkkaV,
    "com.typesafe.akka" %% "akka-persistence" % AkkaV)

  lazy val akkaHttpDeps = Seq(
    "com.typesafe.akka" %% "akka-http" % AkkaHttpV,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpV,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpV)

  lazy val guiceDeps = Seq(
    "net.codingwell" %% "scala-guice" % GuiceV,
    "com.google.inject" % "guice" % GuiceV)

  lazy val testDeps = Seq(
    "org.scalatest" %% "scalatest" % ScalaTestV % "test")

  lazy val demoDeps: Seq[ModuleID] = akkaDeps ++ akkaHttpDeps ++ guiceDeps ++ testDeps
}
