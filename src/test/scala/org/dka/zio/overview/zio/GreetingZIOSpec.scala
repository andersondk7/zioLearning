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

  private val logger     = Logger(getClass.getName)

  private val salutation = "Hello"

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("samples")(
    test("always pass") {
      assertTrue(true)
    },
    test("greeting with logging") {
      for {
        greeting <- ZIO.fromEither(Greeting.apply(salutation, "George")) // left -> fail
        results  <- GreetingEffects.greet(greeting)
      } yield {
        logger.info(s"with logging returned $results")
        assertTrue(
          results.contains(greeting.who),
          results.contains(greeting.salutation)
        )
      }
    },
    test("greeting without logging") {
      val who = "George"
      for {
        mapping <- ZIO.fromEither(Greeting.apply(salutation, who)) // left -> fail
        either  <- ZIO.attempt(Greeting.apply(salutation, who))
        results <- GreetingEffects.show(either)
      } yield {
        logger.info(s"withoutLogging: mapping: $mapping")
        logger.info(s"withoutLogging: either: $either")
        logger.info(s"withoutLogging: results: $results")
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
        logger.info(s"failed message: either: $either")
        logger.info(s"failed message: greeting: $either")
        assertTrue(true)
      }
    }
//
//    test("chaining") {
//      val effect = for {
//        greet <- ZIO.succeed[String]("greetings: ")
//        withName <- ZIO.succeed[String](s"$greet Mr. Smith")
//        withFrom <- ZIO.succeed[String](s" $withName from within ZIO")
//        _ <- ZIO.log(s"final greeting: >$withFrom<")
//      } yield withFrom
//
//      for {
//        result <- effect
//      } yield {
//        logger.info(s"returned: $result")
//        assertTrue(result. contains("from within ZIO"))
//      }
//    }
  )

  //
  // helper methods
  //
  def containsParts(who: String, sal: String): Assertion[String] = Assertion.containsString(who) && containsString(sal)

}
