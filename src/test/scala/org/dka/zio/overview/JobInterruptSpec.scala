package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

import scala.concurrent.duration.*

object JobInterruptSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  private val OneSecondJob = BlockingJob(1.second)

  private val TwoSecondJob = BlockingJob(2.second)

  private val ThreeSecondJob = BlockingJob(3.second)

  private val firstException  = new Exception("first failed")

  private val secondException = new Exception("second  failed")

  private def pause(duration: FiniteDuration = 10.millis): Unit = Thread.sleep(duration.toMillis)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("jobsInterrupt")(
    test("exit interrupt but allow for completion") {
      logger.info("starting run ...")
      for {
        fiber <- BlockingJob.run(TwoSecondJob).fork
        _     <- ZIO.log("fiber has been started")
        exit  <- fiber.interrupt
        _     <- ZIO.log("fiber has been interrupted")
      } yield {
        logger.info(s"exit: $exit")
        assertTrue(exit.isFailure)
      }
    }
  )

}
