package org.dka.zio.overview

import zio.*
import BlockingJob.*
import com.typesafe.scalalogging.Logger
import scala.concurrent.duration.*
import DurationConversions.*

final case class Timing private (start: Long, finish: Option[Long]) {

  def complete: Timing       = this.copy(finish = Some(java.lang.System.currentTimeMillis()))

  val duration: Option[Long] = finish.map(_ - start)

  override def toString: String = s"start: $start, finish: $finish, duration: $duration"

}

object Timing {

  def apply(): Timing = new Timing(
    start = java.lang.System.currentTimeMillis(),
    finish = None
  )

}

/**
 * represents a blocking operation examples: reading a file, query a database etc.
 * @param duration
 *   how long the operation takes
 */
final case class BlockingJob(duration: FiniteDuration)

/**
 * execution of blocking jobs
 * see zio documentation at https://zio.dev/overview/creating-effects#blocking-synchronous-code
 */
object BlockingJob {

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
