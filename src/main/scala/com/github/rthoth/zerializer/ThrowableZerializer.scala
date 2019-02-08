package com.github.rthoth.zerializer

import java.io.{ DataInput, DataOutput, IOException }
import scala.collection.Traversable
import scala.collection.immutable.Seq


class SerializedThrowable(val className: String, message: String, cause: Throwable = null)
    extends RuntimeException(message, cause)

object ThrowableZerializer extends SimpleZerializer[Throwable] {

  def emptyValue = throw new ZerializerException.EmptyValue()

  def isEmpty(value: Throwable) = false

  protected def _read(input: DataInput): SerializedThrowable = {
    val className = input.readUTF()
    val message = input.readUTF()

    val stackSize = input.readInt()

    val stack = if (stackSize > 0) {
      for (_ <- 0 until stackSize) yield {
        val fileName = input.readUTF()
        val className = input.readUTF()
        val methodName = input.readUTF()
        val lineNumber = input.readInt()

        new StackTraceElement(fileName, className, methodName, lineNumber)
      }
    } else if (stackSize == 0) {
      Nil
    } else {
      throw new ZerializerException.Unexpected(s"Invalid Stack Size [$stackSize]!")
    }

    val cause = if (input.readBoolean()) {
      _read(input)
    } else {
      null
    }

    val throwable = new SerializedThrowable(className, message, cause)
    throwable.setStackTrace(stack.toArray)
    throwable
  }

  def read(input: DataInput): Throwable = {
    _read(input)
  }

  protected def serialize(throwable: Throwable, output: DataOutput): Unit = {
    output.writeUTF(throwable.getClass.getCanonicalName)
    output.writeUTF(throwable.getMessage)

    writeStack(throwable.getStackTrace, output)

    if (throwable.getCause != null) {
      output.writeBoolean(true)
      throwable.getCause match {
        case serialized: SerializedThrowable =>
          writeSerialized(serialized, output)

        case cause =>
          serialize(cause, output)
      }
    }
  }

  def write(value: Throwable, output: DataOutput): Unit = {
    value match {
      case serialized: SerializedThrowable =>
        writeSerialized(serialized, output)
      case _ =>
        serialize(value, output)
    }
  }

  protected def writeSerialized(serialized: SerializedThrowable, output: DataOutput): Unit = {
    output.writeUTF(serialized.className)
    output.writeUTF(serialized.getMessage)

    writeStack(serialized.getStackTrace, output)

    if (serialized.getCause != null) {
      output.writeBoolean(true)
      writeSerialized(serialized.getCause.asInstanceOf[SerializedThrowable], output)
    } else {
      output.writeBoolean(false)
    }
  }

  protected def writeStack(stack: Traversable[StackTraceElement], output: DataOutput): Unit = {
    output.writeInt(stack.size)
    for (element <- stack) {
      output.writeUTF(element.getFileName)
      output.writeUTF(element.getClassName)
      output.writeUTF(element.getMethodName)
      output.writeInt(element.getLineNumber)
    }
  }

}
