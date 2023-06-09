package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import org.dka.zio.overview.effects.IntegerResourceEffects
import org.dka.zio.overview.effects.IntegerResourceEffects.*
import zio.*
import zio.test.*
import zio.test.Assertion.*

object ResourceSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  private val updatedValue = 42

  private val initialValue = 120

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("resources")(
    test("fail to acquire a held resource") {
      val heldResource: IntegerResource = IntegerResource(data = initialValue, isHeld = true)
      // create the effect
      val effect: IntegerResourceEffects.ZIOAttempt =
        IntegerResourceEffects.useResource(heldResource) { attempt =>
          // this will never execute because the resource could not be acquired
          // use succeeded because this can not fail
          // don't use ZIO.fromEither because it transforms the Left into the Error type and Right into the Value type
          //  i.e  from ZIO[R, Nothing, A] to ZIO[R, E, A]
          ZIO.succeed(
            attempt.map { resource =>
              resource.data = updatedValue
              resource
            }
          )
        }

      // test the effect
      for {
        result <- effect
      } yield assert(result)(isLeft) && assertTrue(
        heldResource.isHeld,
        heldResource.data == initialValue
      )
    },
    test("modify a free resource") {
      // get the resource, do something on the resource, free the resource
      val resource = IntegerResource(data = initialValue)
      val effect: IntegerResourceEffects.ZIOAttempt =
        IntegerResourceEffects.useResource(resource) { attempt =>
          ZIO.succeed(
            attempt.map { resource =>
              resource.data = updatedValue
              resource
            }
          )
        }

      // run the effect
      for {
        result <- effect
      } yield assert(result)(isRight) && assertTrue(
        result.map(_.data) == Right(updatedValue),
        result.map(_.isHeld) == Right(false)
      )
    },
    test("fail nested acquire") {
      val resource = IntegerResource(data = initialValue)
      val effect: ZIOAttempt =
        ZIO.acquireReleaseWith(IntegerResourceEffects.acquire(resource))(IntegerResourceEffects.release) { attempt =>
          logger.info(s"first $attempt")
          // this one will fail because the resource as already been acquired
          ZIO.acquireReleaseWith(IntegerResourceEffects.acquire(resource))(IntegerResourceEffects.release) { attempt =>
            logger.info(s"second $attempt")
            ZIO.succeed(attempt.map { resource =>
              resource.data = updatedValue
              resource
            })
          }
        }
      for {
        result <- effect
      } yield assert(result)(isLeft)
    }
  )

}
