package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput }


class OptionZerializer[E](underlying: Zerializer[E, E])
    extends SimpleZerializer[Option[E]] {

  val emptyValue: Option[E] = None

  def isEmpty(value: Option[E]) = value.isEmpty

  def read(input: DataInput): Option[E] = {
    if (input.readBoolean())
      Some(underlying.read(input))
    else
      None
  }

  def write(option: Option[E], output: DataOutput): Unit = {
    option match {
      case Some(value) =>
        output.writeBoolean(true)
        underlying.write(value, output)

      case None =>
        output.writeBoolean(false)
    }
  }
}
