package org.dka.zio.overview.effects

import org.dka.zio.overview.IntegerResource
import org.dka.zio.overview.IntegerResource.*
import zio.{UIO, ZIO}

object IntegerResourceEffects {

  type ResourceAttempt = UIO[ResourceErrorsOr]

  def useResource(resource: IntegerResource)(use: ResourceErrorsOr => ResourceAttempt): ResourceAttempt =
    ZIO.acquireReleaseWith(IntegerResourceEffects.acquire(resource))(IntegerResourceEffects.release)(use)

  def acquire(resource: IntegerResource): ResourceAttempt =
    for {
      _ <- ZIO.log(s"acquiring resource with isHeld: ${resource.isHeld} ")
      result =
        if (resource.isHeld) {
          Left(new IllegalStateException(s"can't acquire a held resource: $resource"))
        } else {
          resource.isHeld = true // modify resource
          Right(resource)
        }
      _ <- ZIO.log("finished with acquire")
    } yield result

  def release(attempt: ResourceErrorsOr): ResourceAttempt =
    ZIO.succeed(
      attempt.map { resource =>
        resource.isHeld = false
        resource
      }
    )

}
