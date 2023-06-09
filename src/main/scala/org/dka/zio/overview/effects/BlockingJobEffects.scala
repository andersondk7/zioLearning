package org.dka.zio.overview.effects

import com.typesafe.scalalogging.Logger
import org.dka.zio.overview.{BlockingJob, Timing}
import zio.ZIO

/**
 * execution of blocking jobs see zio documentation at
 * https://zio.dev/overview/creating-effects#blocking-synchronous-code
 */
object BlockingJobEffects {

  // because the methods in this object are 'blocking', we can use this logger inside...
  private val logger = Logger(getClass.getName)

  def run(job: BlockingJob): ZIO[Any, Throwable, Timing] = ZIO.attemptBlockingInterrupt({
    val timing = Timing.apply()
    logger.info(s"job $job starting at ${timing.start}")
    Thread.sleep(job.duration.toMillis) // blocks the job, not the fiber the job is running in
    val completed: Timing = timing.complete
    logger.info(s"job $job completed $completed")
    completed
  })

  def runFail(job: BlockingJob, message: String): ZIO[Any, Throwable, BlockingJob] = ZIO.attemptBlockingInterrupt({
    val timing = Timing.apply()
    logger.info(s"job $job starting at ${timing.start}")
    Thread.sleep(job.duration.toMillis) // blocks the job, not the fiber the job is running in
    throw new Exception(message)
  })

  def runFail(job: BlockingJob, cause: Throwable): ZIO[Any, Throwable, BlockingJob] = ZIO.attemptBlockingInterrupt({
    val timing = Timing.apply()
    logger.info(s"job $job starting at ${timing.start}")
    Thread.sleep(job.duration.toMillis) // blocks the job, not the fiber the job is running in
    logger.info(s"job $job throwing ${cause.getMessage}")
    throw cause
  })

}
