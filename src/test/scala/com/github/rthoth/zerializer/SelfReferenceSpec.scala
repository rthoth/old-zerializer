package com.github.rthoth.zerializer

import java.io._


class SelfReferenceSpec extends Spec {

  case class Envelope(xmin: Double, xmax: Double, ymin: Double, ymax: Double)

  case class QuadNode(id: Long, envelope: Envelope, indexes: Seq[Int],
    _1: Option[QuadNode] = None, _2: Option[QuadNode] = None, _3: Option[QuadNode] = None, _4: Option[QuadNode] = None, parent: QuadNode = null)

  class Context(val fileName: String) {
    val file = new File(s"target/$fileName")

    lazy val output = new FileOutputStream(file)

    lazy val input = new FileInputStream(file)
  }

  "Recursive reference" - {
    try {

      implicit val envelopeSerializer = new ComposedBuilder[Envelope]()
        .field[Double]
        .field[Double]
        .field[Double]
        .field[Double]
        .build(Envelope.apply, Envelope.unapply)

      val quadSerializer: SimpleZerializer[QuadNode] = {
        var serializer = new ComposedBuilder[QuadNode]()
          .field[Long]
          .field[Envelope]
          .field(traversableZerializer[Int, Seq[Int]])
          .field[Option[QuadNode]](StubZerializer)
          .field[Option[QuadNode]](StubZerializer)
          .field[Option[QuadNode]](StubZerializer)
          .field[Option[QuadNode]](StubZerializer)
          .field[QuadNode](StubZerializer)
          .build(QuadNode.apply, QuadNode.unapply)

        val qSerializer = lazyZerializer(serializer)
        val option: SimpleZerializer[Option[QuadNode]] = optionZerializer(qSerializer)

        serializer = serializer.copy(
          _4 = option,
          _5 = option,
          _6 = option,
          _7 = option,
          _8 = qSerializer
        )

        serializer
      }

      val q1 = QuadNode(1, Envelope(0, 1, 0, 1), Seq(0,1,2))
      val q2 = QuadNode(2, Envelope(-1, 0, 0, 1), Seq(0, 2, 3))
      val q3 = QuadNode(3, Envelope(-1, 0, -1, 0), Nil)
      val q4 = QuadNode(4, Envelope(0, 1, -1, 0), Seq(2, 4))

      val q0 = QuadNode(0, Envelope(-1, 1, -1, 1), Seq(3), Some(q1), Some(q4), Some(q3), Some(q4))

      "should serialize/deserialize" in new Context("self-01") {
        quadSerializer.write(q0, output)
        quadSerializer.read(input) should be (q0)
      }
    } catch {
      case cause: Throwable =>
        cause.printStackTrace()
        throw cause
    }
  }
}
