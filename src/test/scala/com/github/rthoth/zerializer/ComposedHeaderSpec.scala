package com.github.rthoth.zerializer

import java.io._

class ComposedHeaderSpec extends Spec {

  private class H(val version: Option[Byte]) {

    lazy val output = new ByteArrayOutputStream
    lazy val dataOutput: DataOutput = new DataOutputStream(output)

    lazy val input = new ByteArrayInputStream(output.toByteArray())
    lazy val dataInput: DataInput = new DataInputStream(input)

    lazy val builder = new ComposedHeader.Builder(version)
    lazy val header = ComposedHeader(version, dataInput)
  }

  "should write" - {

    "with version correctly" in new H(Some(100)) {
      builder
        .status(ComposedHeader.NotEmpty)
        .status(1, ComposedHeader.Null)
        .status(2, ComposedHeader.NotEmpty)
        .status(3, ComposedHeader.Empty)
        .write(dataOutput)

      header.status should be (ComposedHeader.NotEmpty)
      header.status(1) should be (ComposedHeader.Null)
      header.status(2) should be (ComposedHeader.NotEmpty)
      header.status(3) should be (ComposedHeader.Empty)
    }

    "without version correctly" in new H(None) {
      builder
        .status(ComposedHeader.Empty)
        .status(1, ComposedHeader.NotEmpty)
        .status(10, ComposedHeader.Empty)
        .status(13, ComposedHeader.Null)
        .write(dataOutput)

      header.status should be (ComposedHeader.Empty)
      header.status(1) should be (ComposedHeader.NotEmpty)
      header.status(10) should be (ComposedHeader.Empty)
      header.status(13) should be (ComposedHeader.Null)
    }

    "with version correctly null values" in new H(Some(191.toByte)) {
      builder
        .status(ComposedHeader.Null)
        .status(1, ComposedHeader.NotEmpty)
        .status(10, ComposedHeader.Empty)
        .status(20, ComposedHeader.Null)
        .write(dataOutput)

      header.status should be (ComposedHeader.Null)
      header.status(1) should be (ComposedHeader.NotEmpty)
      header.status(10) should be (ComposedHeader.Empty)
      header.status(20) should be (ComposedHeader.Null)
    }
  }
}
