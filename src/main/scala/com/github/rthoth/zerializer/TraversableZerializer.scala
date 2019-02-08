package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput, IOException }
import scala.collection.generic._
import scala.collection._


class TraversableZerializer[E, T <: TraversableOnce[E]](underlying: Zerializer[E, E])(implicit canBuild: CanBuild[E, T])
    extends SimpleZerializer[T] {

  val emptyValue: T = canBuild().result()

  def isEmpty(value: T) = value.isEmpty

  def read(input: DataInput): T = {
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
  
  def write(value: T, output: DataOutput): Unit = {
    output.writeInt(value.size)

    for (e <- value)
      underlying.write(e, output)
  }
}
