package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import zio.{ZIO, *}

// in the acquire/release cycle in zio, the resource acquired is the same instance
// as the one passed to the release function.
// these are typically such resources as:
//  sockets
//  files
//  (db connections???)
//
// therefore, the resource must be mutable
//
// as such you can't use a case class to represent a resource (since it is mutable)
// so the implementations are all mutable classes ...

final case class IntegerResource(var isHeld: Boolean = false,
                            var data: Int = 0
                           ) { }


object IntegerResource {
  private val logger = Logger(getClass.getName)



  type ZioAttempt = ZIO[Any, Nothing, Either[IllegalStateException, IntegerResource]]

  def acquire(resource: IntegerResource): ZioAttempt = {
    logger.info(s"acquiring resource: $resource")
    ZIO.succeed {
      if (resource.isHeld) {
        Left(new IllegalStateException(s"can't acquire a held resource: $resource"))
      } else {
        resource.isHeld = true // modify resource
        Right(resource)
      }
    }
  }

  def release( attempt: Either[IllegalStateException, IntegerResource] ): ZioAttempt = {
    logger.info(s"releasing attempt: $attempt")
    val action = attempt.map(resource => {
      resource.isHeld = false
      resource
    })
    logger.info(s"letting go of: $action")

    ZIO.succeed(action)
  }
}
