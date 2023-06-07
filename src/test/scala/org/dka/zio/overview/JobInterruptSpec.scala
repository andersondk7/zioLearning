package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

import scala.concurrent.duration.*

object JobInterruptSpec extends ZIOSpecDefault {

  private val logger         = Logger(getClass.getName)

  private val OneSecondJob   = Job(1.second)

  private val TwoSecondJob   = Job(2.second)

  private val ThreeSecondJob = Job(3.second)

  private val firstException = new Exception("first failed")
  private val secondException = new Exception("second  failed")

  private def pause(duration: FiniteDuration = 10.millis): Unit = Thread.sleep(duration.toMillis)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("jobsInterrupt") (

    test("exit interrupt but allow for completion") {
      for {
        fiber <- Job.run(TwoSecondJob).fork
        exit  <- fiber.interrupt
      } yield {
        logger.info(s"exit: $exit")
        println(s"\n*********************\n")
        println(s"\n*********************\n")
        println(s"\n*********************\n")
        println(s"exit:  $exit")
        println(s"\n*********************\n")
        println(s"\n*********************\n")
        println(s"\n*********************\n")
        //
        // note:  interrupted is not the same as canceled.
        // even though the fiber was interrupted, it still runs to completion
        //
        // if you look at the logger statements (both inside the job and in the test)
        // you will see that the job finishes before the yield executes
        // see comment in https://zio.dev/overview/basic-concurrency
        //   By design, the effect returned by Fiber#interrupt does not resume until the
        //   fiber has completed, which helps ensure your code does not spin up new fibers
        //   until the old one has terminated.
        // not sure why one would want to interrupt a thread that completes anyway
        //  I did not see the 'immediate termination of the fiber'
        //
        assertTrue(
          exit.isSuccess,
        exit.exists(_.finish.isDefined)
        )
      }
    }

  )

}
