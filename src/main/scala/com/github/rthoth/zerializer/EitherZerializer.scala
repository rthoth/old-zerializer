package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput }


class EitherZerializer[L, R](lZerializer: Zerializer[L, L], rZerializer: Zerializer[R, R]) extends SimpleZerializer[Either[L, R]] {

  def emptyValue = throw new ZerializerException.EmptyValue

  def isEmpty(value: Either[L, R]) = false

  def read(input: DataInput): Either[L, R] = {
    input.readBoolean() match {
      case false =>
        Left(lZerializer.read(input))

      case true =>
        Right(rZerializer.read(input))
    }
  }

  def write(value: Either[L, R], output: DataOutput): Unit = {
    value match {
      case Left(value) =>
        output.writeBoolean(false)
        lZerializer.write(value, output)

      case Right(value) =>
        output.writeBoolean(true)
        rZerializer.write(value, output)
    }
  }
}
