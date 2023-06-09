package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import scala.concurrent.duration.*
import DurationConversions.*
import cats.Show

final case class Timing private (start: Long, finish: Option[Long]) {

  def complete: Timing = this.copy(finish = Some(java.lang.System.currentTimeMillis()))

  val duration: Option[Long] = finish.map(_ - start)

}

object Timing {

  def apply(): Timing = new Timing(
    start = java.lang.System.currentTimeMillis(),
    finish = None
  )

  implicit val showTiming: Show[Timing] =
    Show.show(timing => s"start: ${timing.start}, finish: ${timing.finish}, duration: ${timing.duration}")

}

/**
 * represents a blocking operation examples: reading a file, query a database etc.
 * @param duration
 *   how long the operation takes
 */
final case class BlockingJob(duration: FiniteDuration)
