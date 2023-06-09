package org.dka.zio.overview.effects

import org.dka.zio.overview.IntegerResource
import org.dka.zio.overview.IntegerResource.*
import zio.ZIO

object IntegerResourceEffects {


  type ZIOAttempt = ZIO[Any, Nothing, ResourceErrorsOr]

  def useResource(resource: IntegerResource)(use: ResourceErrorsOr => ZIOAttempt): ZIOAttempt =
    ZIO.acquireReleaseWith(IntegerResourceEffects.acquire(resource))(IntegerResourceEffects.release)(use)

  def acquire(resource: IntegerResource): ZIOAttempt =
    for {
      _ <- ZIO.log(s"acquiring resource with isHeld: ${resource.isHeld} ")
      result =  if (resource.isHeld) {
        Left(new IllegalStateException(s"can't acquire a held resource: $resource"))
      } else {
        resource.isHeld = true // modify resource
        Right(resource)
      }
      _ <- ZIO.log("finished with acquire")
    } yield result

  def release(attempt: ResourceErrorsOr): ZIOAttempt =
    ZIO.succeed(
      attempt.map { resource =>
        resource.isHeld = false
        resource
      }
    )

}
