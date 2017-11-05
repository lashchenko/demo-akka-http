import sbt._
import Dependencies._

lazy val root = (project in file("."))
  .settings(
    name := "demo-akka-http",
    scalaVersion := ScalaV,
    libraryDependencies ++= demoDeps
  )
