package org.dka.zio.state

import zio.*
import zio.test.*
import zio.test.Assertion.*

object LoggingSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("logging")(

    test("always pass") {
      assertTrue( true )
    },

    test("logging output to Console") {
      val names = List("Ann", "Bill", "Susan", "Sam", "Nick")
      for {
        logger <- ConsoleLogger.make()
        count <- new Processor(logger).processNames(names)
      } yield {
        assertTrue(count == names.length)
      }
    }

  )
}
