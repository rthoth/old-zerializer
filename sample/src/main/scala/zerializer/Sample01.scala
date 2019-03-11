package zerializer

import com.github.rthoth.zerializer._
import java.io._
import scala.collection.immutable.{ Queue, SortedMap }
import scala.util.{ Failure, Try }

object Sample01 extends App {

  case class User(name: String, email: String, age: Int, active: Boolean)

  implicit val userSerializer = new ComposedBuilder()
    .field[String]
    .field[String]
    .field[Int]
    .field[Boolean]
    .build(User.apply, User.unapply)

  val ownUserSerializer = new ComposedBuilder()
    .field[String]
    .field[String]
    .field[Int]
    .field[Boolean]
    .rw {
      (name, email, age, active) => User(name, email, age, active)
    } {
      user => if (user != null)
        Some((user.name, user.email, user.age, user.active))
      else
        None
    }

  case class Sale(id: Long, cost: Double, owner: User)

  val saleSerializer = new ComposedBuilder()
    .field[Long]
    .field[Double]
    .field[User]
    .build(Sale.apply, Sale.unapply)


  val user = User("Albert", "e@m.c2", 50, true)

  userSerializer.write(user, new FileOutputStream("target/user-01"))
  assert(userSerializer.read(new FileInputStream("target/user-01")) == user)

  val sale = Sale(599, 0.56, user)
  saleSerializer.write(sale, new FileOutputStream("target/sale-01"))
  assert(saleSerializer.read(new FileInputStream("target/sale-01")) == sale)

  // Traversables and Maps

  case class Element(name: String, number: Int, family: String, electrons: List[Int])

  case class PeriodicTable(name: String, elements: Queue[Element], elementByNumber: SortedMap[Int, Element])

  implicit val elementZerializer = new ComposedBuilder(Some(120))
    .field[String]
    .field[Int]
    .field[String]
    .field(traversableZerializer[Int, List])
    .build(Element.apply, Element.unapply)

  implicit val periodicTableZerializer = new ComposedBuilder(Some(20))
    .field[String]
    .field(traversableZerializer[Element, Queue])
    .field(mapZerializer[Int, Element, SortedMap])
    .build(PeriodicTable.apply, PeriodicTable.unapply)

  val hydrogen = Element("Hydrogen", 1, "IA", 1 :: Nil)

  val oxygen = Element("Oxygen", 8, "VIA", 2 :: 6 :: Nil)

  val table = PeriodicTable("My Table", Queue(oxygen, hydrogen), SortedMap(8 -> oxygen, 1 -> hydrogen))

  periodicTableZerializer.write(table, new FileOutputStream("target/table"))

  val serializedTable = periodicTableZerializer.read(new FileInputStream("target/table"))
  assert(serializedTable == table)


  case class Tag(name: String, attributes: Map[String, String], children: Seq[Tag])

  implicit val tagZerializer = {
    var ret: SimpleZerializer[Tag] = null

    implicit val serializer = lazyZerializer(ret)

    ret = new ComposedBuilder()
      .field[String]
      .field(mapZerializer[String, String, Map])
      .field(traversableZerializer[Tag, Seq])
      .build(Tag.apply, Tag.unapply)

    ret
  }

  val html = Tag("html", Map("lang" -> "pt_BR"),
    Tag("head", Map.empty, Nil) :: Tag("body", Map("onLoad" -> "null"), Nil) :: Nil)

  tagZerializer.write(html, new FileOutputStream("target/tag"))
  val serializedTag = tagZerializer.read(new FileInputStream("target/tag"))
  assert(serializedTag == html)


  case class Essay(title: String, author: String, content: Option[String], approved: Try[Boolean], score: Either[String, Int])

  val essayZerializer = new ComposedBuilder()
    .field[String]
    .field[String]
    .field[Option[String]]
    .field[Try[Boolean]]
    .field[Either[String, Int]]
    .build(Essay.apply, Essay.unapply)

  val essay = Essay("My little star!", "!!!", None, Failure(new IllegalStateException("Ouch!")), Left("I don't like it"))

  essayZerializer.write(essay, new FileOutputStream("target/essay"))
  val serializedEssay = essayZerializer.read(new FileInputStream("target/essay"))

  // == does not work on Throwable!
  val Failure(serializedEssayCause) = serializedEssay.approved
  val Failure(essayCause) = essay.approved
  assert(serializedEssayCause.getStackTrace.mkString == essayCause.getStackTrace.mkString)
  assert(serializedEssay.content == essay.content)
  assert(serializedEssay.score == essay.score)
}
