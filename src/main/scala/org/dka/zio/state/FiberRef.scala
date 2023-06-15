package org.dka.zio.state

import org.dka.zio.state.Logging.*
import zio.*

trait Logging {

  /**
   * log a message and annotations to some medium
   */
  def log(message: String): UIO[Unit]

  /**
   * apply annotation to logging in the block of code
   */
  def annotate[R, E, A](key: String, value: String)(block: ZIO[R, E, A]): ZIO[R, E, A]

  /**
   * apply annotation to logging in the block of code
   */
  def annotate[R, E, A](annotations: Annotations)(block: ZIO[R, E, A]): ZIO[R, E, A]

}

object Logging {
  type Annotations = Map[String, String]
}

/**
 * implementation of Logging that writes to the console
 *   note: the FiberRef[Annotations] is scoped
 *         this means that adding to that ref (i.e. adding an annotation)
 *         is only valid during the body of the zio passed due to the
 *         acquire/release semantics of scope
 *
 * @param ref annotations added to the logging output
 */
final case class ConsoleLogger private(ref: FiberRef[Annotations]) extends Logging {

  override def log(message: String): UIO[Unit] = ref.get.flatMap {
    case annotation if annotation.isEmpty => Console.printLine(message).orDie
    case map                              => Console.printLine(withAnnotations(map, message)).orDie
  }

  override def annotate[R, E, A](annotations: Annotations)(zio: ZIO[R, E, A]): ZIO[R, E, A] = {
    ref.locallyWith(annotation => annotation ++ annotations )(zio)
  }

  override def annotate[R, E, A](key: String, value: String)(zio: ZIO[R, E, A]): ZIO[R, E, A] = {
    ref.locallyWith(annotation => annotation.updated(key, value))(zio)
  }

  private def withAnnotations(annotation: Annotations, message: String): String = {
    val annotations = annotation
      .map { case (k, v) =>
        s"[$k = $v]"
      }
      .mkString(" ")
    s"$annotations $message"
  }

}

object ConsoleLogger {

  def make(): ZIO[Scope, Nothing, ConsoleLogger] = FiberRef.make(Map.empty[String, String]).map(new ConsoleLogger(_))

}

class Processor(logging: Logging) {

  def processNames(names: List[String]): ZIO[Any, Nothing, Int] = for {
    _ <- logging.log("Processing async...")
    _ <- ZIO.foreachParDiscard(names)(processRequest) //each happens in its own fiber
    _ <- logging.log("All requests processed")
  } yield names.length

  private def processRequest(request: String): UIO[Any] = {
    for {
      fiberId <- ZIO.fiberId.map(_.ids.head)
      _ <- logging.annotate( Map(
          "request" ->  request,
          "fiberId" -> fiberId.toString
        )) {
        for {
          _ <- logging.log(s"Processing ...")
          _ <- processName(request)
          _ <- logging.log("Finished processing")
        } yield ()
      }
    } yield ()
  }

  private def processName(name: String): UIO[Unit] =
    logging.annotate("function", "processName") {
      logging.log(s"processing:  $name")
  }

}
