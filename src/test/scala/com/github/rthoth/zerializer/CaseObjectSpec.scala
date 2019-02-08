package com.github.rthoth.zerializer

import java.io._
import scala.collection.immutable.{ Map, Queue }




class CaseObjectSpec extends Spec {

  class T {
    lazy val output = new ByteArrayOutputStream()
    lazy val dataOutput: DataOutput = new DataOutputStream(output)
    lazy val input = new ByteArrayInputStream(output.toByteArray())
    lazy val dataInput: DataInput = new DataInputStream(input)
  }

  case class User(name: String, email: String, age: Byte)
  case class Sale(id: Long, ready: Boolean, owner: Option[User], mapping: Map[Int, User])
  case class SaleList(owner: User, inviteds: List[User], sales: Queue[Sale])

  "should write/read case classes" - {
    implicit val userZerializer = new ComposedBuilder[User]
      .field[String]
      .field[String]
      .field[Byte]
      .build(User.apply, User.unapply)

    implicit val saleZerializer = new ComposedBuilder[Sale]
      .field[Long]
      .field[Boolean]
      .field[Option[User]]
      .field(mapField[Int, User, Map[Int, User]])
      .build(Sale.apply, Sale.unapply)

    implicit val saleListZerializer = new ComposedBuilder[SaleList](Some(102))
      .field[User]
      .field(traversableField[User, List[User]])
      .field(traversableField[Sale, Queue[Sale]])
      .build(SaleList.apply, SaleList.unapply)

    val user = User("Einstein", "e@m.c2", 10)

    "user" in new T {
      userZerializer.write(user, dataOutput)
      userZerializer.read(dataInput) should be(user)
    }

    val sale = Sale(100L, true, Some(user), Map(10  -> user, 12 -> null))

    "sale" in new T {
      saleZerializer.write(sale, dataOutput)
      saleZerializer.read(dataInput) should be (sale)
    }

    val saleList = SaleList(user, user :: user :: null :: Nil, Queue(sale, null, sale, null))

    "sale list" in new T {
      saleListZerializer.write(saleList, dataOutput)
      saleListZerializer.read(dataInput) should be (saleList)
    }
  }
}
