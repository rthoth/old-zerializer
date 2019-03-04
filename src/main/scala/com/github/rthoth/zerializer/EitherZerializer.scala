package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput }


class EitherZerializer[L, R](lZ: Zerializer[L, L], rZ: Zerializer[R, R]) extends SimpleZerializer[Either[L, R]] {

  def emptyValue = throw new ZerializerException.EmptyValue

  def isEmpty(value: Either[L, R]) = false

  def read(input: DataInput): Either[L, R] = {
    if (input.readBoolean()) {
      Left(lZ.read(input))
    } else {
      Right(rZ.read(input))
    }
  }

  def write(value: Either[L, R], output: DataOutput): Unit = {
    value match {
      case Left(value) =>
        output.writeBoolean(false)
        lZ.write(value, output)

      case Right(value) =>
        output.writeBoolean(true)
        rZ.write(value, output)
    }
  }
}
