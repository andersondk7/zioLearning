package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import IntegerResource.*
import zio.*
import zio.test.*
import zio.test.Assertion.*

object ResourceSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)
  private val updatedValue = 42
  private val initialValue = 120

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("resources") (

    test("fail to acquire a held resource") {
      val heldResource: IntegerResource = IntegerResource(data = initialValue, isHeld = true)
      val effect: IntegerResource.ZioAttempt =
        ZIO.acquireReleaseWith(IntegerResource.acquire(heldResource))(IntegerResource.release) { attempt =>
          // the effect
          // this will never execute because the resource could not be acquired
          logger.info(s"updating held $attempt")
          val updated =
            attempt.map { resource =>
              resource.data = updatedValue
              resource
            }
          ZIO.succeed(updated)
        }

      // run the effect
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
      val effect: IntegerResource.ZioAttempt =
        ZIO.acquireReleaseWith(IntegerResource.acquire(resource))(IntegerResource.release) { attempt =>
          // the effect
          logger.info(s"updating free $attempt")
          val updated =
            attempt.map { resource =>
              resource.data = updatedValue
              resource
            }
          ZIO.succeed(updated)
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
      val effect: ZioAttempt = ZIO.acquireReleaseWith(IntegerResource.acquire(resource))(IntegerResource.release) {
        attempt =>
          logger.info(s"first $attempt")
          // this one will fail because the resource as already been acquired
          ZIO.acquireReleaseWith(IntegerResource.acquire(resource))(IntegerResource.release) { attempt =>
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
