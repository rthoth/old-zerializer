package com.github.rthoth.zerializer

import java.io._
import com.github.rthoth.zerializer._

object ReflectionThrowableZerializerSpec {
  class MyException(message: String, cause: Throwable = null) extends RuntimeException(message, cause)
}

import ReflectionThrowableZerializerSpec._

class ReflectionThrowableZerializerSpec extends Spec {

  class T {
    lazy val output = new ByteArrayOutputStream()
    lazy val dataOutput: DataOutput = new DataOutputStream(output)

    lazy val input = new ByteArrayInputStream(output.toByteArray())
    lazy val dataInput: DataInput = new DataInputStream(input)

    lazy val serializer = new ReflectionThrowableZerializer()
  }

  "should write correctly" - {

    "a simple IOException" in new T {

      val exception = new IOException("A simple IO Exception")
      serializer.write(exception, dataOutput)
      serializer.read(dataInput) should haveSameStack (exception)
    }

    "a custom exception with cause" in new T {
      val illegal = new IllegalArgumentException("Illegal")
      val exception = new MyException("Ops!", illegal)

      serializer.write(exception, dataOutput)
      serializer.read(dataInput) should haveSameStack(exception)
    }
  }
}
