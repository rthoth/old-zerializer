package com.github.rthoth.zerializer

import java.io._
import java.lang
import java.lang.reflect.Constructor
import scala.annotation.tailrec
import scala.collection.immutable.Seq


class ReflectionThrowableZerializer(cl: ClassLoader = Thread.currentThread().getContextClassLoader) extends SimpleZerializer[Throwable] {

  def emptyValue: Throwable = new ZerializerException.EmptyValue

  def isEmpty(value: Throwable) = false

  protected def newInstance(constructor: Constructor[_], message: String,
    stack: Seq[StackTraceElement], cause: Option[Throwable]): Throwable = {

    val instance = cause match {
      case Some(value) => constructor.newInstance(message, value).asInstanceOf[Throwable]
      case None => constructor.newInstance(message).asInstanceOf[Throwable]
    }

    instance.setStackTrace(stack.toArray)
    instance
  }

  final def read(input: DataInput): Throwable = {
    if (input.readBoolean()) {
      val clazz = cl.loadClass(input.readUTF())
      val message = input.readUTF()
      val stack = readStack(input)
      val cause = read(input)

      try {
        newInstance(clazz.getConstructor(classOf[String], classOf[Throwable]), message, stack, Some(cause))
      } catch {
        case _: NoSuchMethodException =>
          newInstance(clazz.getConstructor(classOf[String]), message, stack, None)
      }
    } else {
      null
    }
  }

  protected def readStack(input: DataInput): Seq[StackTraceElement] = {
    for (_ <- 0 until input.readInt()) yield {
      val className = input.readUTF()
      val methodName = input.readUTF()
      val filename = input.readUTF()
      val line = input.readInt()
      new StackTraceElement(className, methodName, filename, line)
    }
  }

  final def write(value: Throwable, output: DataOutput): Unit = {
    if (value != null) {
      output.writeBoolean(true)
      output.writeUTF(value.getClass.getName)
      output.writeUTF(value.getMessage)
      writeStack(value.getStackTrace.toList, output)
      write(value.getCause, output)
    } else {
      output.writeBoolean(false)
    }
  }

  protected def writeStack(stack: Seq[StackTraceElement], output: DataOutput): Unit = {
    output.writeInt(stack.size)
    for (element <- stack) {
      output.writeUTF(element.getClassName)
      output.writeUTF(element.getMethodName)
      output.writeUTF(element.getFileName)
      output.writeInt(element.getLineNumber)
    }
  }
}
