package org.dka.zio.overview

import cats.Show
import org.dka.zio.overview.IntegerResource.ResourceErrorsOr

/**
 * represents a mutable resource that must be 'held' before it can be modified
 * in the acquire/release cycle in zio, the resource acquired is the same instance
 * as the one passed to the release function.
 * these are typically such resources as:
 *  sockets
 *  files
 *  (db connections???)
 *
 * therefore, the resource must be mutable
 *
 * todo: consider separating the resource being held and the holder of the resource
 *  that way the resource can be immutable, but the holder does not need to be
 *  i.e. the holder has a resource that can be swapped with a new one, but the
 *     instance of the holder is the same
 */     

final case class IntegerResource(var isHeld: Boolean = false, var data: Int = 0) {}

object IntegerResource {
  type ResourceErrorsOr = Either[Throwable, IntegerResource]

  def apply(data: Int): IntegerResource = new IntegerResource(false, data)

  implicit val showIntegerResource: Show[IntegerResource] = Show.show(ir => s"held: ${ir.isHeld},  data: ${ir.data}")

}
