import sbt.*

object Dependencies {

  private val cats_version = "2.9.0"
  private val circe_version = "0.14.5"
  private val logback_version = "1.4.6"
  private val scalalogging_version = "3.9.5"
  private val zio_version = "2.0.13"

  private val catsCore = "org.typelevel" %% "cats-core" % cats_version

  private val circeCore = "io.circe" %% "circe-core" % circe_version
  private val circeGeneric = "io.circe" %% "circe-generic" % circe_version
  private val circeParser = "io.circe" %% "circe-parser" % circe_version

  private val logging = "com.typesafe.scala-logging" %% "scala-logging" % scalalogging_version


  // java libs
  private val logBack = "ch.qos.logback" % "logback-classic" % logback_version


  // zio libs
  private val zio = "dev.zio" %% "zio" % zio_version
  private val zioTest = "dev.zio" %% "zio-test" % zio_version % Test
  private val zioTestSbt = "dev.zio" %% "zio-test-sbt" % zio_version % Test
  private val zioTestMagnolia = "dev.zio" %% "zio-test-magnolia" % zio_version % Test

  val zioDependencies: Seq[ModuleID] = Seq(
    circeCore,
    circeGeneric,
    circeParser,
    logging,
    logBack,
    zio,
    zioTest,
    zioTestSbt,
    zioTestMagnolia
  )
}
