package org.dka.zio.overview.effects

import cats.Show
import cats.syntax.all.*
import zio.{UIO, ZIO}

import org.dka.zio.overview.Greeting
import org.dka.zio.overview.Greeting.GreetingErrorsOr

object GreetingEffects {

  import Greeting.*

  /**
   * shows the greeting in a ZIO effect
   */
  def show(greeting: Greeting): UIO[String] = ZIO.succeed(greeting.show)

  def show(errorsOr: GreetingErrorsOr): UIO[String] = ZIO.succeed(errorsOr match {
    case Left(error)     => error.getMessage
    case Right(greeting) => greeting.show
  })

  /**
   * implements show as a ZIO effect with internal logging
   */
  def greet(greeting: Greeting): UIO[String] = for {
    _       <- ZIO.log(s"hello called for $greeting")
    message <- ZIO.succeed(greeting.show)
    _       <- ZIO.log(s"hello finished for $greeting")
  } yield message

}
