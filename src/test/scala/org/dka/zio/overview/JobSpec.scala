package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

import scala.concurrent.duration.*

object JobSpec extends ZIOSpecDefault {

  private val logger         = Logger(getClass.getName)

  private val OneSecondJob   = BlockingJob(1.second)

  private val TwoSecondJob   = BlockingJob(2.second)

  private val ThreeSecondJob = BlockingJob(3.second)

  private val firstException = new Exception("first failed")
  private val secondException = new Exception("second  failed")

  private def pause(duration: FiniteDuration = 10.millis): Unit = Thread.sleep(duration.toMillis)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("jobs") (

    test("single job") {
      for {
        fiber  <- BlockingJob.run(OneSecondJob).fork
        result <- fiber.join
      } yield assertTrue(result.finish.isDefined)
    },
    
    test("wait on single job") {
      for {
        fiber  <- BlockingJob.run(OneSecondJob).fork
        exit   <- fiber.await
        result <- fiber.join
      } yield {
        logger.info(s"exit: $exit")
        logger.info(s"result: $result")
        assertTrue(exit.isSuccess)
        assertTrue(result.finish.isDefined)
      }
    },
    
    test("exit success") {
      for {
        fiber  <- BlockingJob.run(OneSecondJob).fork
        exit   <- fiber.await
        result <- fiber.join
      } yield {
        logger.info(s"exit: $exit")
        logger.info(s"result: $result")
        assertTrue(exit.isSuccess)
      }
    },
    
    test("exit interrupt immediately") {
      for {
        fiber <- BlockingJob.run(TwoSecondJob).fork
        _ = pause // allow the thread to get going before interruption
        exit <- fiber.interrupt.fork
      } yield {
        logger.info(s"interrupt complete, fiber still running")
        //
        // note:  interrupted is not the same as canceled.
        // even though the fiber was interrupted, it still runs to completion
        // but in this case the execution of the effect ( the for statement )
        //   terminates before the fiber completes
        //
        // eventually the logging statement from the fiber will show up in the console...
        //
        pause(500.millis) // to capture the logging statements of the running thread
        logger.info(s"waiting ${java.lang.System.currentTimeMillis()}...")
        pause(500.millis) // to capture the logging statements of the running thread
        logger.info(s"waiting ${java.lang.System.currentTimeMillis()}...")
        pause(500.millis) // to capture the logging statements of the running thread
        logger.info(s"waiting ${java.lang.System.currentTimeMillis()}...")
        pause(500.millis) // to capture the logging statements of the running thread
        logger.info(s"waiting ${java.lang.System.currentTimeMillis()}...")
        pause(500.millis) // to capture the logging statements of the running thread
        logger.info(s"waiting ${java.lang.System.currentTimeMillis()}...")
        //
        // at this point the fiber is still running (as seen by the logging statements)
        // but we have no way to access it (exit is a unit)
        // unless we fiber.join inside the for
        //
        // by now the fiber has completed ...
        assertTrue(true)
      }
    },
    
    test("fiber fails with message") {
      val message = "expected"
      for {
        j1     <- BlockingJob.runFail(OneSecondJob, message).fork
        result <- j1.join.either
      } yield {
        logger.info(s"result: $result")
        assertTrue(
          result.isLeft,
          result match {
            case Left(ex) => ex.getMessage == message
            case Right(_) => false
          })
      }
    },
    
    test("fiber fails with cause") {
      val cause = new Exception("expected")
      for {
        j1     <- BlockingJob.runFail(OneSecondJob, cause).fork
        result <- j1.join.either
      } yield {
        logger.info(s"result: $result")
        assert(result)(isLeft(equalTo(cause)))
      }
    },
    
    test("compose with zip") {
      for {
        j1 <- BlockingJob.run(OneSecondJob).fork
        j2 <- BlockingJob.run(OneSecondJob).fork
        concurrentJobs = j1.zip(j2) // no need for zipPar since they are already running in parallel
        tuple <- concurrentJobs.join
      } yield {
        logger.info(s"tuple: $tuple")
        assertTrue(tuple._1.finish.isDefined)
        assertTrue(tuple._2.finish.isDefined)
        assertTrue(tuple._2.start < tuple._1.finish.get) // j2 started before j1 finished
      }
    },
    
    test("compose first longer than second") {
      for {
        j1 <- BlockingJob.run(TwoSecondJob).fork
        j2 <- BlockingJob.run(OneSecondJob).fork
        fiber = j1.orElse(j2)
        result <- fiber.join
      } yield {
        assertTrue(result.finish.isDefined)
        // j1 is a 2 second job, should finish in roughly 2000 millis (but more than 1200 millis)
        // j2 is a 1 second job, should finish in roughly 1000 millis (but definitely less than 1200 millis)
        assertTrue(result.duration.get > 1200L)
      }
    },
    
    test("compose first shorter than second") {
      for {
        j1 <- BlockingJob.run(OneSecondJob).fork
        j2 <- BlockingJob.run(TwoSecondJob).fork
        fiber = j1.orElse(j2)
        result <- fiber.join
      } yield {
        assertTrue(result.finish.isDefined)
        // j1 is a 1 second job, should finish in roughly 1000 millis (but definitely less than 1200 millis)
        // j2 is a 2 second job, should finish in roughly 2000 millis (but more than 1200 millis)
        assertTrue(result.duration.get < 1200L)
        //
        // note: from the logger output, we see that even though the first is completed,
        //  the effect waits for both to complete before returning
        //
      }
    },
    
    test("compose first fail") {
      val message = "expected"
      for {
        j1 <- BlockingJob.runFail(ThreeSecondJob, message).fork // will fail after about 1.5 seconds
        _ = Thread.sleep(10)
        j2 <- BlockingJob.run(OneSecondJob).fork
        fiber = j1.orElse(j2)
        result <- fiber.join
      } yield {
        logger.info(s"result: $result")
        assert(result)(isSubtype[Timing](anything))
        val timing = result.asInstanceOf[Timing]
        assertTrue(
          timing.duration.isDefined,  // job completed
          timing.duration.get < 1100L // job completed in roughly 1000 millis
        )
      }
    },
    
    test("compose first and second fail") {
      for {
        j1 <- BlockingJob.runFail(ThreeSecondJob, firstException).fork // will fail after about 1.5 seconds
        _ = Thread.sleep(10)
        j2 <- BlockingJob.runFail(OneSecondJob, secondException).fork
        fiber = j1.orElse(j2)
        result <- fiber.join.either
      } yield {
        logger.info(s"result: $result")
        assert(result)(isLeft(equalTo(secondException)))
      }
    },
    
    test("take the first completed") {
      val longer = BlockingJob.run(TwoSecondJob)
      val shorter = BlockingJob.run(OneSecondJob)
      for {
        result <- longer.race(shorter) // jobs start here!
      } yield {
        logger.info(s"result: $result")
        assertTrue(result.finish.isDefined,
          result.duration.get < 1100L // the OneSecondJob
        )
      }
    },
    
    test("take the first completed of multiple") {
      val j3 = BlockingJob.run(ThreeSecondJob)
      val j2 = BlockingJob.run(TwoSecondJob)
      val j1 = BlockingJob.run(OneSecondJob)
      for {
        result   <- j3.race(j2).race(j1) // jobs start here!
      } yield {
        logger.info(s"result: $result")
        assertTrue(result.finish.isDefined,
          result.duration.get < 1100L // the OneSecondJob
        )
      }
    },
    
    test("take the first success when one fails of multiple") {
      val j3 = BlockingJob.run(ThreeSecondJob)
      val j2 = BlockingJob.run(TwoSecondJob)
      val j1 = BlockingJob.runFail(OneSecondJob, new Exception("first failed"))
      for {
        result   <- j3.race(j2).race(j1) // jobs start here!
      } yield {
        assert(result)(isSubtype[Timing](anything))
        val timing = result.asInstanceOf[Timing]
        assertTrue (
          timing.finish.isDefined,
          timing.duration.get > 1100L, // the OneSecondJob failed
          timing.duration.get < 2100L // the TwoSecondJob is the one that would succeed
        )
      }
    },
    
    test("take the first success or failure when one fails of multiple") {
      // the race method works on effects, not fibers...
      val j3 = BlockingJob.run(ThreeSecondJob)
      val j2 = BlockingJob.run(TwoSecondJob)
      val j1 = BlockingJob.runFail(OneSecondJob, firstException).either // needed to catch exception
      for {
        result <- j3.either.race(j2.either).race(j1) // jobs start here!
      } yield {
        logger.info(s"result: $result")
        assert(result)(isLeft(equalTo(firstException)))
      }
    }
  )
}
