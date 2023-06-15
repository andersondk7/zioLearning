package org.dka.zio.stm
import zio._
import zio.stm._

trait MoneyTransfer {

  protected def transferSTM(from: TRef[Long], to: TRef[Long], amount: Long): STM[String, Long]

  val errorMessage = "Not enough money"
  def createAmount(amount: Long): IO[Nothing, TRef[Long]] = STM.atomically(TRef.make(amount))
  def transfer(from: TRef[Long], to: TRef[Long], amount: Long): IO[String, Long] = STM.atomically(transferSTM(from, to, amount))
}

object FailFastTransfer extends MoneyTransfer {

  def transferSTM(from: TRef[Long], to: TRef[Long], amount: Long): STM[String, Long] =
    for {
      initialBalance <- from.get
      _         <- if (initialBalance < amount) STM.fail(errorMessage)
      else STM.unit
      _         <- from.update(existing => existing - amount)
      _         <- to.update(existing => existing + amount)
      finalBalance   <- to.get
    } yield finalBalance
}

object RetryTransfer extends MoneyTransfer {
  def transferSTM(from: TRef[Long], to: TRef[Long], amount: Long): STM[String, Long] =
    for {
      initialBalance <- from.get
      _         <- if(initialBalance < amount) STM.retry else STM.unit
//      _ <- STM.check(initialBalance < amount) // retries when underlying data structures change
      _         <- STM.check(initialBalance < amount) // retries when underlying data structures change
      _         <- from.update(existing => existing - amount)
      _         <- to.update(existing => existing + amount)
      finalBalance   <- to.get
    } yield finalBalance
}
