package org.dka.zio.overview

import cats.Show

/**
 * Greeting
 * @param who // can't be empty
 */
final case class Greeting private (salutation: String, who: String) {
  def withSalutation(s: String): Greeting = this.copy(salutation = s)
}

object Greeting {

  type GreetingErrorsOr = Either[Throwable, Greeting]

  /**
   * 'who' can't be empty
   */
  def apply(salutation: String, who: String): GreetingErrorsOr =
    if (who.isBlank) Left(new IllegalArgumentException("who can not be empty"))
    else Right(new Greeting(salutation, who))

  val default: Greeting = new Greeting("hello", "???")

  implicit val showGreeting: Show[Greeting] =
    Show.show(greeting => s"salutation: ${greeting.salutation}, who: ${greeting.who}")

}
