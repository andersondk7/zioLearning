package org.dka.zio.overview

import com.typesafe.scalalogging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

object PlaceHolderSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("placeHolder")(
    test("placeholder") {
      assertTrue(true)
    }
  )

}
