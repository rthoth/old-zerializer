package com.github.rthoth.zerializer

import java.io.{ PrintWriter, StringWriter }
import org.scalatest.matchers.{ MatchResult, Matcher }
import org.scalatest.{ FreeSpec, Matchers }

object Spec {

  class ThrowableStackMatcher(expected: Throwable) extends Matcher[Throwable] {

    def apply(left: Throwable): MatchResult = {
      val expectedStack = stackOf(expected)
      val leftStack = stackOf(left)

      MatchResult(
        expectedStack == leftStack,
        s"$leftStack was not equal to $expectedStack",
        s"Inesperado!"
      )
    }

    protected def stackOf(throwable: Throwable): String = {
      val buffer = new StringWriter()
      throwable.printStackTrace(new PrintWriter(buffer))
      buffer.toString()
    }
  }

}

abstract class Spec extends FreeSpec with Matchers {

  protected def haveSameStack(x: Throwable): Spec.ThrowableStackMatcher = {
    new Spec.ThrowableStackMatcher(x)
  }
}
