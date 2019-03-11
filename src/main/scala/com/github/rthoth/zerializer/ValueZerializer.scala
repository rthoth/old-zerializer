package com.github.rthoth.zerializer
import java.io.{DataInput, DataOutput}

class ValueZerializer[V](value: V) extends SimpleZerializer[V] {

  override def emptyValue: V = {
    throw new ZerializerException.EmptyValue()
  }

  override def isEmpty(value: V): Boolean = {
    false
  }

  override def read(input: DataInput): V = {
    value
  }

  override def write(value: V, output: DataOutput): Unit = {

  }
}
