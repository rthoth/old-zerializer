package com.github.rthoth.zerializer

abstract class AnyValZerializer[E <: AnyVal] extends SimpleZerializer[E] {

  def emptyValue = throw new ZerializerException.EmptyValue()

  def isEmpty(x: E): Boolean = false
}
