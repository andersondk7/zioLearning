package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

object FinalizerSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("finalizers") (
    test("placeholder") {
      for {
        x <- ZIO.succeed("placeholder")
      } yield {
        assertTrue(true)
      }
    },

    test("finalizer on error") {
      val finalizer: UIO[Unit] = ZIO.succeed(println(s"*******\nfinalizer called\n"))
      val finalized: ZIO[Any, Throwable, String] = ZIO.fail(new Exception("Failed!")).ensuring(finalizer)

      for {
        x <- finalized.orElseEither(ZIO.succeed("failed first"))
      } yield {
        assertTrue(true)
      }
    },
    
    test("finalizer on success") {
      val finalizer: UIO[Unit] = ZIO.succeed(println(s"*******\nfinalizer called\n"))
      val finalized: ZIO[Any, Throwable, String] = ZIO.attempt("succeeded!").ensuring(finalizer)

      for {
        x <- finalized.orElseEither(ZIO.succeed("failed first"))
      } yield {
        assertTrue(true)
      }
    }
  )
}
