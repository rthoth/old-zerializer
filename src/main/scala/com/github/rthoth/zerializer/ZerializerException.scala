package com.github.rthoth.zerializer

import java.io.IOException


object ZerializerException {

  class EmptyValue(message: String = "", cause: Throwable = null)
      extends IOException(message, cause)

  class Header(message: String, cause: Throwable = null)
      extends IOException(message, cause)

  class NullValue(message: String = "", cause: Throwable = null)
      extends IOException(message, cause)

  class Unexpected(message: String, cause: Throwable = null)
      extends IOException(message, cause)

  class Stub() extends IOException("")
}
