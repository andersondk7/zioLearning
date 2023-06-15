package org.dka.zio.stm

import zio.*
import zio.stm.*
import zio.test.*
import zio.test.Assertion.*

object MoneyTransferSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment with Scope, Any] = suite("logging")(
    test("successful transfer") {
      val impl: MoneyTransfer = FailFastTransfer
      val attempt: IO[String, Long] = for {
        from <- impl.createAmount(1000)
        to <- impl.createAmount(2000)
        balance <- impl.transfer(from, to, 500)
      } yield balance
      attempt.fold(
        error => assertTrue(true),
        b => assertTrue(b == 2500)
      )
    },

    test("unsuccessful transfer") {
      val impl: MoneyTransfer = FailFastTransfer
      val attempt: IO[String, Long] = for {
        from <- impl.createAmount(250)
        to <- impl.createAmount(2000)
        balance <- impl.transfer(from, to, 500)
      } yield balance
      attempt.fold(
        error => assertTrue(error == impl.errorMessage),
        b => assertTrue(false)
      )
    }
    ,

    test("retry successful") {
//      val impl: MoneyTransfer = RetryTransfer
      val impl: MoneyTransfer = FailFastTransfer
      val attempt = for {
        a1 <- impl.createAmount(250)
        a2 <- impl.createAmount(2000)
        a3 <- impl.createAmount(1000)
        t1 <- impl.transfer(a1, a2, 500) // will initially fail
        t2 <- impl.transfer(a3, a1, 500) // now a1 has enough money, t1 will succeed
      } yield t2

      attempt.fold(
        error => assertTrue(false),
        b => assertTrue(b == 2500)
      )
    }
  )

}
