package com.github.rthoth.zerializer

import scala.collection.immutable._

class MultiTypeSpec extends Spec {

  trait Animal

  trait Mammal extends Animal

  trait Bird extends Animal

  case class Dog(name: String) extends Mammal

  case class Cat(name: String) extends Mammal

  case class Seagull(name: String) extends Bird

  case class Dove(name: String) extends Bird

  val dogSerializer = new ComposedBuilder[Dog]().field[String].build(Dog.apply, Dog.unapply)
  val catSerializer = new ComposedBuilder[Cat]().field[String].build(Cat.apply, Cat.unapply)
  val seagullSerializer = new ComposedBuilder[Seagull]().field[String].build(Seagull.apply, Seagull.unapply)
  val doveSerializer = new ComposedBuilder[Dove]().field[String].build(Dove.apply, Dove.unapply)

  "Should serialize" - {

    "mammals" in {

      val zerializer = new MultiZerializer.Builder[Mammal]
        .register(0, dogSerializer)
        .register(1, catSerializer)
        .build()

      val dog = zerializer.write(Dog("Dog!"))
      val cat = zerializer.write(Cat("Cat!"))

      zerializer.read(dog) should be(Dog("Dog!"))
      zerializer.read(cat) should be(Cat("Cat!"))
    }

    "birds" in {
      val zerializer = new MultiZerializer.Builder[Bird]
        .register(0, doveSerializer)
        .register(10, seagullSerializer)
        .build()

      val dove = zerializer.write(Dove("Buu"))
      val seagull = zerializer.write(Seagull("Meno"))

      zerializer.read(dove) should be (Dove("Buu"))
      zerializer.read(seagull) should be (Seagull("Meno"))
    }

    "animals" in {
      val zerializer = new MultiZerializer.Builder[Animal]
        .register(10, doveSerializer)
        .register(11, seagullSerializer)
        .register(20, dogSerializer)
        .register(21, catSerializer)
        .build()

      val dog = zerializer.write(Dog("D1"))
      val cat = zerializer.write(Cat("C0"))
      val dove = zerializer.write(Dove("D0"))
      val seagull = zerializer.write(Seagull("S9"))

      zerializer.read(seagull) should be (Seagull("S9"))
      zerializer.read(dove) should be (Dove("D0"))
      zerializer.read(cat) should be (Cat("C0"))
      zerializer.read(dog) should be (Dog("D1"))
    }
  }
}
