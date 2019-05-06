package com.github.rthoth.zerializer

import java.io.{DataInput, DataOutput}

import scala.util.{Failure, Success, Try}

class TryZerializer[E](underlying: Zerializer[E, E], throwableSerializer: Zerializer[Throwable, Throwable])
    extends SimpleZerializer[Try[E]] {

  def emptyValue = throw new ZerializerException.EmptyValue()

  def isEmpty(value: Try[E]) = false

  def read(input: DataInput): Try[E] = {
    if (input.readBoolean()) {
      Success(underlying.read(input))
    } else {
      Failure(throwableSerializer.read(input))
    }
  }

  def write(value: Try[E], output: DataOutput): Unit = {
    value match {
      case Success(content) =>
        output.writeBoolean(true)
        underlying.write(content, output)

      case Failure(cause) =>
        output.writeBoolean(false)
        throwableSerializer.write(cause, output)
    }
  }
}
