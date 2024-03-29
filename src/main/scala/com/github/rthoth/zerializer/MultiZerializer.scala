package com.github.rthoth.zerializer

import java.io.{DataInput, DataOutput}

import com.github.rthoth.zerializer.MultiZerializer._

import scala.collection.immutable.{HashMap, Map}


object MultiZerializer {

  protected class Entry[+T](val index: Int, val clazz: Class[_], val serializer: Zerializer[Any, Any])

  class Builder[T] private(entries: Map[Int, Entry[T]]) {

    def this() = this(HashMap.empty)

    def register[V <: T : Manifest](index: Int, serializer: Zerializer[V, V]): Builder[T] = {
      if (!entries.contains(index)) {
        new Builder(entries + (index -> new Entry(index, manifest[V].runtimeClass, serializer.asInstanceOf[Zerializer[Any, Any]])))
      } else {
        throw new IllegalArgumentException(index.toString)
      }
    }

    def register[V <: T : Manifest](index: Int, value: V): Builder[T] = {
      register(index, new ValueZerializer(value))
    }

    def build(): MultiZerializer[T] = new MultiZerializer(entries)
  }
}


class MultiZerializer[T](entries: Map[Int, Entry[T]])
  extends SimpleZerializer[T] {

  private val reversed = HashMap(entries.map(x => (x._2.clazz.getCanonicalName, x._2)).toSeq: _*)

  def emptyValue: T = throw new ZerializerException.EmptyValue()

  def isEmpty(value: T) = false

  def read(input: DataInput): T = {
    if (input.readBoolean()) {
      val hash = input.readInt()
      entries.get(hash) match {
        case Some(entry) => entry.serializer.read(input).asInstanceOf[T]
        case None => throw new ZerializerException.Unexpected(s"Type [$hash]!")
      }

    } else {
      null.asInstanceOf[T]
    }
  }

  def write(value: T, output: DataOutput): Unit = {
    if (value != null) {
      output.writeBoolean(true)

      val clazz = value.getClass.getCanonicalName

      reversed.get(clazz) match {
        case Some(entry) =>
          output.writeInt(entry.index)
          entry.serializer.write(value, output)

        case None =>
          throw new ZerializerException.Unexpected(clazz)
      }

    } else {
      output.writeBoolean(false)
    }
  }

}
