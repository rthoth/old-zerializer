package zerializer

import com.github.rthoth.zerializer._
import java.io._

object Sample01 extends App {

  case class User(name: String, email: String, age: Int, active: Boolean)

  implicit val userSerializer = new TypeBuilder()
    .field[String]
    .field[String]
    .field[Int]
    .field[Boolean]
    .build(User.apply, User.unapply)

  val ownUserSerializer = new TypeBuilder()
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

  val saleSerializer = new TypeBuilder()
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
}
