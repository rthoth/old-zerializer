package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput, IOException }

object ComposedHeader {

  sealed abstract class Status(val flag: Long)

  case object NotEmpty extends Status(0x2L)

  case object Empty extends Status(0x1L)

  case object Null extends Status(0x0L)

  class Builder(version: Option[Byte]) {

    private var control = 0L

    def status(status: Status): Builder = {
      val flag = status.flag

      control ^= (control & 0x3)
      control |= flag

      this
    }

    def status(field: Int, status: Status): Builder = {
      check(field > 0 && field <= 22, new ZerializerException.Header(s"Invalid field [$field]!"))

      control ^= (control & (0x3L << (2 * field)))
      control |= (status.flag << (2 * field))

      this
    }

    def status(field: Int): Status = {
      check(field > 0 && field <= 22, throw new ZerializerException.Header(s"Invalid field [$field]!"))

      ((control >> (2 * field)) & 0x3) match {
        case NotEmpty.flag => NotEmpty
        case Null.flag => Null
        case Empty.flag => Empty
        case flag => throw new ZerializerException.Header(s"Invalid flag [$flag] for field [$field]!")
      }
    }

    def write(output: DataOutput): Unit = {
      control ^= (control & (0xFFL << 46))

      if (version.isDefined) {
        control |= (version.get.toLong << 46)
      }

      output.writeLong(control)
    }
  }

  def apply(version: Option[Byte], input: DataInput): ComposedHeader = {
    new ComposedHeader(version, input.readLong())
  }
}

import ComposedHeader._

class ComposedHeader(version: Option[Byte], control: Long) {

  if (version.isDefined) {
    val value = ((control >> 46) & 0xFFL).toByte
    check(value == version.get,
      new ZerializerException.Header(s"Invalid version [${value}], it was expected [${version.get}]!"))
  }

  def status: Status = {
    (control & 0x3) match {
      case NotEmpty.flag => NotEmpty
      case Null.flag => Null
      case Empty.flag => Empty
      case flag => throw new ZerializerException.Header(s"Invalid flag [$flag] for object!")
    }
  }

  def status(field: Int): Status = {
    check(field > 0 && field <= 22, throw new ZerializerException.Header(s"Invalid field [$field]!"))
      ((control >> (field * 2)) & 0x3) match {
      case NotEmpty.flag => NotEmpty
      case Null.flag => Null
      case Empty.flag => Empty
      case flag => throw new ZerializerException.Header(s"Invalid flag [$flag] for field [$field]!")
    }
  }
}
