package com.github.rthoth.zerializer

import scala.collection.immutable._
import java.io._

object EnumZerializer {

  class Builder[E] private (values: HashMap[Int, E]) {

    def this() = this(HashMap.empty)

    def register[T >: E](id: Int, value: T): Builder[T] = {
      if (!values.contains(id)) {
        new Builder(values + (id -> value))
      } else {
        throw new ZerializerException.Unexpected(s"Enum [$id] has already registered!")
      }
    }

    def build(): EnumZerializer[E] = new EnumZerializer(values)
  }
}


class EnumZerializer[E] private (values: Map[Int, E]) extends SimpleZerializer[E] {

  private val reversed = values.map(_.swap)

  def emptyValue = throw new ZerializerException.EmptyValue()

  def isEmpty(value: E) = false

  def read(input: DataInput): E = {
    val id = input.readInt()
    values.get(id) match {
      case Some(value) => value
      case None => throw new ZerializerException.Unexpected(s"Unexpected id [$id]!")
    }
  }

  def write(value: E, output: DataOutput): Unit = {
    reversed.get(value) match {
      case Some(id) =>
        output.writeInt(id)
      case None => throw new ZerializerException.Unexpected(s"Unexpected value [$value]!")
    }
  }

}
