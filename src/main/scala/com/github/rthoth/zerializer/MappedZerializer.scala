package com.github.rthoth.zerializer

import java.io.{DataInput, DataOutput}

class MappedZerializer[E, M](toSerializer: E => M, fromSerializer: M => E, serializer: Zerializer[M, M])
    extends SimpleZerializer[E] {

  def emptyValue: E = fromSerializer(serializer.emptyValue)

  def isEmpty(value: E) = serializer.isEmpty(toSerializer(value))

  def read(input: DataInput): E = {
    fromSerializer(serializer.read(input))
  }

  def write(value: E, output: DataOutput): Unit = {
    serializer.write(toSerializer(value), output)
  }
}
