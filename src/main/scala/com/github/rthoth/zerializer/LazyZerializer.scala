package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput }


class LazyZerializer[E](factory: => Zerializer[E, E]) extends SimpleZerializer[E] {

  private lazy val underlying = factory

  def emptyValue = underlying.emptyValue

  def isEmpty(value: E) = underlying.isEmpty(value)

  def read(input: DataInput): E = underlying.read(input)

  def write(value: E, output: DataOutput): Unit = underlying.write(value, output)
}
