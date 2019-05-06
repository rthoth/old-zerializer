package com.github.rthoth.zerializer

import java.io._

trait Zerializer[-I, +R] {

  def emptyValue: R

  def isEmpty(value: I): Boolean

  def read(input: DataInput): R

  def read(bytes: Array[Byte]): R = {
    read(new DataInputStream(new ByteArrayInputStream(bytes)) : DataInput)
  }

  def read(input: InputStream): R = input match {
    case dataInput: DataInput => read(dataInput : DataInput)
    case _ => read(new DataInputStream(input) : DataInput)
  }

  def write(value: I, output: DataOutput): Unit

  def write(value: I): Array[Byte] = {
    val output =  new ByteArrayOutputStream(64)
    write(value, new DataOutputStream(output) : DataOutput)
    output.toByteArray()
  }

  def write(value: I, output: OutputStream): Unit = output match {
    case dataOutput: DataOutput => write(value, dataOutput : DataOutput)
    case _ => write(value, new DataOutputStream(output) : DataOutput)
  }
}

/** Zerializer with just one type parameter. */
trait SimpleZerializer[E] extends Zerializer[E, E]
