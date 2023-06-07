package org.dka.zio.overview

import zio.Scope
import zio.*
import zio.test.*
import zio.test.Assertion.*

object SamplesSpec extends ZIOSpecDefault {
  private val salutation: String = "greetings"
  private val generator = new Message(salutation)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("samples") (

    test("always pass") {
      assertTrue(true)
    },

    test("simple message") {
      val who = "George"
      for {
        generated <- generator.greet(who)
      } yield assert(generated)(isRight(containsParts(who, salutation)))
    },

    test("simple failed message") {
      val who = ""
      for {
        generated <- generator.greet(who)
      } yield assert(generated)(isLeft(isSubtype[IllegalArgumentException](anything)))
    }
  )

  //
  // helper methods
  //
  def containsParts(who: String, sal: String): Assertion[String] = Assertion.containsString(who) && containsString(sal)

}
