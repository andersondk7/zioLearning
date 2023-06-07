package org.dka.zio.overview

import zio.*

class Message(salutation: String) {

  def hello: UIO[String] = for {
    childFiber  <- ZIO.succeed(s"$salutation!").fork
    parentFiber <- childFiber.join
  } yield parentFiber

  def greet(who: String): UIO[Either[Throwable, String]] = ZIO.succeed(
    if (who.isBlank) Left(new IllegalArgumentException("who can not be empty"))
    else Right(s"$salutation $who")
  )

}
