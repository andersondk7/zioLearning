package org.dka.zio.overview.zio

import com.typesafe.scalalogging.Logger
import zio.*
import zio.test.*
import zio.test.Assertion.*

object PlaceHolderZIOSpec extends ZIOSpecDefault {

  private val logger = Logger(getClass.getName)

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("placeHolder")(
    test("placeholder") {
      assertTrue(true)
    }
  )

}
