package org.dka.zio.overview.zio

import cats.Show
import cats.syntax.all.*
import com.typesafe.scalalogging.Logger
import org.dka.zio.overview.Greeting
import org.dka.zio.overview.Greeting.GreetingErrorsOr
import org.dka.zio.overview.effects.GreetingEffects
import zio.*
import zio.test.*
import zio.test.Assertion.*

object GreetingZIOSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  private val salutation = "Hello"
  val who = "George"
  val blank = ""

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("samples")(
    test("always pass") {
      assertTrue(true)
    },
    test("greeting with logging") {
      for {
        greeting <- ZIO.fromEither(Greeting.apply(salutation, "George")) @@ ZIOAspect.debug(
          "after after step: fromEither") @@ ZIOAspect.annotated("step", "fromEither")
        // debug debug prints _after_ the effect runs
        // annotated prints _during_ the run of the effect and appends 'key=value' to the log message
        results <- GreetingEffects.greet(greeting) @@ ZIOAspect.annotated("step", "greet")
      } yield assertTrue(
        results.contains(greeting.who),
        results.contains(greeting.salutation)
      )
    },

    test("greeting without logging") {
      for {
        mapping <- ZIO.fromEither(Greeting.apply(salutation, who)) // left -> fail, but this won't fail...
        either  <- ZIO.attempt(Greeting.apply(salutation, who)) @@ ZIOAspect.logged("aspectLogging: ")
        results <- GreetingEffects.show(either)
      } yield {
        logger.info(s"withoutLogging yield: mapping: $mapping")
        logger.info(s"withoutLogging yield: either: $either")
        logger.info(s"withoutLogging yield: results: $results")
        assertTrue(
          results.contains(who),
          results.contains(salutation)
        )
      }
    },

    test("catching failed message") {
      //
      // the fromEither takes an Either[A, B] and converts to ZIO[Any, A, B]
      // another approach is rather than return a ZIO[Any, Throwable, Greeting] and deal with pseudo try/catch semantics
      // return ZIO[Any, Nothing, Either[Throwable, Greeting]
      // I prefer ZIO[Any, Nothing, Either[Throwable, Greeting]] and dealing with the either as you keep the type for later use
      //
      for {
        message <- ZIO.fromEither(Greeting.apply(salutation, "")).catchAll { // returns left or right (no mapping)
          case iae: IllegalArgumentException =>
            val msg = s"caught IllegalArgumentException: ${iae.getMessage}"
            ZIO.log(msg)
            ZIO.succeed(msg)

          case e: Throwable =>
            val msg = s"caught IllegalArgumentException: ${e.getMessage}"
            ZIO.log(msg)
            ZIO.succeed(msg)
        }.map(x => x.toString)
      } yield {
        logger.info(s"greeting: $message")
        assertTrue(true)
      }
    },
    test("catching successful message") {
      //
      // the fromEither takes an Either[A, B] and converts to ZIO[Any, A, B]
      // another approach is rather than return a ZIO[Any, Throwable, Greeting] and deal with pseudo try/catch semantics
      // return ZIO[Any, Nothing, Either[Throwable, Greeting]
      // I prefer ZIO[Any, Nothing, Either[Throwable, Greeting]] and dealing with the either as you keep the type for later use
      //
      for {
        message <- ZIO.fromEither(Greeting.apply(salutation, who)).catchAll { // returns left or right (no mapping)
          case iae: IllegalArgumentException =>
            val msg = s"caught IllegalArgumentException: ${iae.getMessage}"
            ZIO.log(msg)
            ZIO.succeed(msg)

          case e: Throwable =>
            val msg = s"caught IllegalArgumentException: ${e.getMessage}"
            ZIO.log(msg)
            ZIO.succeed(msg)
        }.map(x => x.toString)
      } yield {
        logger.info(s"greeting: $message")
        assertTrue(true)
      }
    },

    test("simple failed message") {
      for {
        either   <- ZIO.attempt(Greeting.apply(salutation, blank)) // returns left or right (no mapping)
        _        <- ZIO.log(s"received $either")
        greeting <- GreetingEffects.show(either)
      } yield {
        assert(either)(isLeft(isSubtype[IllegalArgumentException](anything)))
        assertTrue(greeting.contains("can not be empty"))
      }
    },

    test("exit method") {
      for {
        exit <- ZIO.attempt(Greeting.apply(salutation, who)).exit
      } yield {
        assert(exit)(isSubtype[Exit[Nothing, GreetingErrorsOr]](anything))
        assertTrue(exit match {
          case Exit.Success(errorsOr) =>
            errorsOr.isRight &&
              errorsOr.map(g => g.who == who).getOrElse(false) &&
              errorsOr.map(g => g.salutation == salutation).getOrElse(false)

          case Exit.Failure(cause) => false
        })
      }
    },

    test("cause method") {
      //
      // note: orDie returns a Defect:  a situation not expected, but still needs to be
      // propagated up until they can be handled
      // Defects are caught with #catchAllDefect or #catchSomeDefect
      // whereas failures (ZIO.fail(...) ) are caught with #catchAll or #catchSome
      //
      for {
//        cause <- ZIO.fromEither(Greeting.apply(salutation, blank)).orDie.cause
        cause <- ZIO.fromEither(Greeting.apply(salutation, blank)).orDie.catchAllCause {
          case Cause.Empty =>
            ZIO.debug("no error caught")
          case Cause.Fail(v, _) =>
            ZIO.debug(s"a failure caught: $v")
          case Cause.Die(value, _) =>
            ZIO.debug(s"a defect(die) caught: $value")
          case Cause.Interrupt(fiberId, _) =>
            ZIO.debug(s"a fiber interruption caught with the fiber id: $fiberId")
          case Cause.Stackless(cause: Cause.Die, _) =>
            ZIO.debug(s"a stackless defect caught: ${cause.value}")
          case Cause.Stackless(cause: Cause[_], _) =>
            ZIO.debug(s"an unknown stackless defect caught: ${cause.squashWith(identity)}")
          case Cause.Then(left, right) =>
            ZIO.debug(s"two consequence causes caught")
          case Cause.Both(left, right) =>
            ZIO.debug(s"two parallel causes caught")
        }
      } yield {
        logger.info(s"cause: $cause")
//        cause match {
//          case Cause.Empty => logger.info(s"got empty cause")
//          case Cause.Fail(value, _) => logger.info(s"got fail cause with $value")
//          case Cause.Die(ex, _) => logger.info(s"got Die cause with $ex")
//          case x => logger.info(s"got cause: $x")
//        }
        assertTrue(true)
//        assertTrue(!cause.isEmpty)
      }
    }
  )

  //
  // helper methods
  //
  def containsParts(who: String, sal: String): Assertion[String] = Assertion.containsString(who) && containsString(sal)

}
