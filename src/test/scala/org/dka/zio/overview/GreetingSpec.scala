package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import org.scalatest.funspec.*
import org.scalatest.matchers.should.Matchers

class GreetingSpec extends AnyFunSpec with Matchers {

  private val logger = Logger(getClass.getName)

  private val salutation = "Hello"

  private val validWho = "George"

  private val invalidWho = ""

  describe("construction") {
    it("should construct with a valid 'who'") {
      val construct = Greeting.apply(salutation, validWho)
      construct match {
        case Left(err) => fail(err)
        case Right(greeting) =>
          greeting.who shouldBe validWho
          greeting.salutation shouldBe salutation
      }
    }
    it("should not construct with an invalid 'who'") {
      val construct = Greeting.apply(salutation, invalidWho)
      construct match {
        case Left(err)       => succeed
        case Right(greeting) => fail(s"should no construct with an invalid 'who'")
      }
    }
  }

}
