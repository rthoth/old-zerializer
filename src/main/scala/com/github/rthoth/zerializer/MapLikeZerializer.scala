package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput, IOException }
import scala.collection._
import scala.collection.generic._

class MapLikeZerializer[K, V, M[A, B] <: MapLike[A, B, _]](kZerializer: Zerializer[K, K], vZerializer: Zerializer[V, V])(implicit canBuild: CanBuild[(K, V), M[K, V]])
    extends SimpleZerializer[M[K, V]] {

  lazy val emptyValue: M[K, V] = canBuild.apply().result()

  def isEmpty(value: M[K, V]) = value.isEmpty

  def read(input: DataInput): M[K, V] = {
    val size = input.readInt()

    if (size > 0) {

      val builder = canBuild()

      for (_ <- 0 until size) {
        val k = kZerializer.read(input)
        val v = vZerializer.read(input)

        builder += k -> v
      }

      builder.result()
    } else if (size == 0) {
      canBuild().result()
    } else {
      throw new IOException(s"Invalid size [$size]!")
    }
  }

  def write(value: M[K, V], output: DataOutput): Unit = {
    output.writeInt(value.size)

    for ((k, v) <- value) {
      kZerializer.write(k, output)
      vZerializer.write(v, output)
    }
  }
}
