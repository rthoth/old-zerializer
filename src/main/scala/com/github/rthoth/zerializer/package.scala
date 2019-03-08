package com.github.rthoth

import java.io.{DataInput, DataOutput, IOException}
import scala.collection.immutable.HashMap
import scala.language.implicitConversions
import scala.collection._
import scala.collection.generic._
import scala.util.Try

package object zerializer {

  implicit lazy val BooleanZerializer: SimpleZerializer[Boolean] = new AnyValZerializer[Boolean] {
    def read(input: DataInput): Boolean = input.readBoolean()

    def write(value: Boolean, output: DataOutput): Unit = output.writeBoolean(value)
  }

  implicit lazy val ByteZerializer: SimpleZerializer[Byte] = new AnyValZerializer[Byte] {
    def read(input: DataInput): Byte = input.readByte()

    def write(value: Byte, output: DataOutput): Unit = output.writeByte(value)
  }

  implicit lazy val CharZerializer: SimpleZerializer[Char] = new AnyValZerializer[Char] {
    def read(input: DataInput): Char = input.readChar()

    def write(value: Char, output: DataOutput): Unit = output.writeChar(value)
  }

  implicit lazy val DoubleZerializer: SimpleZerializer[Double] = new AnyValZerializer[Double] {
    def read(input: DataInput): Double = input.readDouble()

    def write(value: Double, output: DataOutput): Unit = output.writeDouble(value)
  }

  implicit lazy val FloatZerializer: SimpleZerializer[Float] = new AnyValZerializer[Float] {
    def read(input: DataInput): Float = input.readFloat()

    def write(value: Float, output: DataOutput): Unit = output.writeFloat(value)
  }

  implicit lazy val IntZerializer: SimpleZerializer[Int] = new AnyValZerializer[Int] {
    def read(input: DataInput): Int = input.readInt()

    def write(value: Int, output: DataOutput): Unit = output.writeInt(value)
  }

  implicit lazy val LongZerializer: SimpleZerializer[Long] = new AnyValZerializer[Long] {
    def read(input: DataInput): Long = input.readLong()

    def write(value: Long, output: DataOutput): Unit = output.writeLong(value)
  }

  implicit lazy val ShortZerializer: SimpleZerializer[Short] = new AnyValZerializer[Short] {
    def read(input: DataInput): Short = input.readShort()

    def write(value: Short, output: DataOutput): Unit = output.writeShort(value)
  }

  implicit lazy val StringZerializer: SimpleZerializer[String] = new SimpleZerializer[String] {

    val emptyValue = ""

    def isEmpty(value: String): Boolean = value.isEmpty()

    def read(input: DataInput): String = input.readUTF()

    def write(value: String, output: DataOutput): Unit = output.writeUTF(value)
  }

  @inline
  def check(condition: Boolean, throwable: => IOException): Unit = {
    if (!condition)
      throw throwable
  }

  implicit def eitherZerializer[L, R](
    implicit lZerializer: Zerializer[L, L], rZerializer: Zerializer[R, R]
  ): SimpleZerializer[Either[L, R]] = {
    new EitherZerializer(lZerializer, rZerializer)
  }

  implicit def optionZerializer[E](
    implicit serializer: Zerializer[E, E]
  ): SimpleZerializer[Option[E]] = {
    new OptionZerializer(serializer)
  }

  implicit def tryZerializer[E](
    implicit serializer: Zerializer[E, E],
    throwableZerializer: Zerializer[Throwable, Throwable] = new ReflectionThrowableZerializer
  ): SimpleZerializer[Try[E]] = {

    new TryZerializer(serializer, throwableZerializer)
  }

  def mapZerializer[K, V, M <: MapLike[K, V, _]](
    implicit kZ: Zerializer[K, K], vZ: Zerializer[V, V], canBuild: CanBuild[(K, V), M]
  ): SimpleZerializer[M] = {

    new MapLikeZerializer(kZ, vZ)
  }

  def traversableZerializer[E, T <: TraversableOnce[E]](
    implicit serializer: Zerializer[E, E], canBuild: CanBuild[E, T]
  ): SimpleZerializer[T] = {

    new TraversableZerializer(serializer)
  }

  implicit def throwableZerialzer(
    implicit cl: ClassLoader = Thread.currentThread().getContextClassLoader
  ): SimpleZerializer[Throwable] = {

    new ReflectionThrowableZerializer(cl)
  }

  def mappedZerializer[E, M](to: E => M)(from: M => E)(
    implicit serializer: Zerializer[M, M]
  ): SimpleZerializer[E] = {
    new MappedZerializer(to, from, serializer)
  }

  def lazyZerializer[E](serializer: => Zerializer[E, E]): SimpleZerializer[E] = {
    new LazyZerializer(serializer)
  }
}
