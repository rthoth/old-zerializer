package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput }


object StubZerializer extends Zerializer[Any, Nothing] {

  def emptyValue = throw new ZerializerException.Stub

  def isEmpty(value: Any) = throw new ZerializerException.EmptyValue

  def read(input: DataInput): Nothing = throw new ZerializerException.Stub

  def write(value: Any, output: DataOutput): Unit = throw new ZerializerException.Stub
}
