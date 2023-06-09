package org.dka.zio.overview.zio

import cats.Show
import cats.syntax.all.*
import com.typesafe.scalalogging.Logger
import org.dka.zio.overview.Greeting
import org.dka.zio.overview.effects.GreetingEffects
import zio.*
import zio.test.*
import zio.test.Assertion.*

object GreetingZIOSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  private val salutation = "Hello"

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
      val who = "George"
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
    test("simple failed message") {
      val who = ""
      for {
        either   <- ZIO.attempt(Greeting.apply(salutation, who)) // returns left or right (no mapping)
        _        <- ZIO.log(s"received $either")
        greeting <- GreetingEffects.show(either)
      } yield {
        assert(either)(isLeft(isSubtype[IllegalArgumentException](anything)))
        assertTrue(greeting.contains("can not be empty"))
      }
    }
  )

  //
  // helper methods
  //
  def containsParts(who: String, sal: String): Assertion[String] = Assertion.containsString(who) && containsString(sal)

}
