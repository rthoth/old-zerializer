package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput, IOException }
import scala.collection.generic._
import scala.collection._

class TraversableZerializer[E, T[X] <: TraversableOnce[X]](underlying: Zerializer[E, E])(implicit canBuild: CanBuild[E, T[E]])
    extends SimpleZerializer[T[E]] {

  val emptyValue: T[E] = canBuild().result()

  def isEmpty(value: T[E]) = value.isEmpty

  def read(input: DataInput): T[E] = {
    val size = input.readInt()

    if (size > 0) {
      val builder = canBuild()

      for (_ <- 0 until size) {
        builder += underlying.read(input)
      }

      builder.result()
    } else if (size == 0) {
      canBuild().result()
    } else {
      throw new ZerializerException.Unexpected(s"Invalid size [$size]!")
    }
  }
  
  def write(value: T[E], output: DataOutput): Unit = {
    output.writeInt(value.size)

    for (e <- value)
      underlying.write(e, output)
  }
}
