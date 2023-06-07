package org.dka.zio.overview

import zio.*
import Job.*
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration.*

final case class Timing private (start: Long, finish: Option[Long]) {
  def complete: Timing = this.copy(finish = Some(java.lang.System.currentTimeMillis()))
  val duration: Option[Long] = finish.map(_ - start)

  override def toString: String = s"start: $start, finish: $finish, duration: $duration"
}
object Timing {
  def apply(): Timing = new Timing(
    start = java.lang.System.currentTimeMillis(),
    finish = None
  )

}
final case class Job(duration: FiniteDuration)


object Job {
  private val logger = Logger(getClass.getName)


  def run(job: Job): ZIO[Any, Throwable, Timing] = ZIO.succeed({
    val timing = Timing.apply()
    logger.info(s"job $job starting at ${timing.start}")
    Thread.sleep(job.duration.toMillis)
    val completed: Timing = timing.complete
    logger.info(s"job $job completed $completed")
    completed
  })

  def runFail(job: Job, message: String): ZIO[Any, Throwable, Job] = ZIO.attempt({
    val timing = Timing.apply()
    logger.info(s"job $job starting at ${timing.start}")
    Thread.sleep(job.duration.toMillis / 2)
    throw new Exception(message)
  })

  def runFail(job: Job, cause: Throwable): ZIO[Any, Throwable, Job] = ZIO.attempt( {
    val timing = Timing.apply()
    logger.info(s"job $job starting at ${timing.start}")
    Thread.sleep(job.duration.toMillis / 2)
    logger.info(s"job $job throwing ${cause.getMessage}")
    throw cause
  })
}