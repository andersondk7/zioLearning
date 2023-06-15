import Dependencies._

lazy val scala3 = "3.3.0"

ThisBuild / organization := "org.dka.zio"
ThisBuild / version := "0.1.1"
ThisBuild / scalaVersion := scala3

lazy val zio = project
  .in(file("."))
  .configs(IntegrationTest)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "zio",
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    libraryDependencies ++= zioDependencies,
    Defaults.itSettings
  )
