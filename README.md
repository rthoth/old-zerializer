# Zerializer

**Zerializer** is a Scala based binary serialization library, it uses [DataInput](https://docs.oracle.com/javase/8/docs/api/index.html?java/io/DataInput.html "See javadoc") and [DataOutput](https://docs.oracle.com/javase/8/docs/api/index.html?java/io/DataOutput.html "See javadoc"). So, let's code!

First we need import:

```Scala
import com.github.rthoth.zerializer._
```

## Case Classes

Case classes can to simplify your work, because they provide some *magic methods* like **apply** and **unapply**. But **Zerializer** does not force you to use **case classes**.

```scala
  case class User(name: String, email: String, age: Int, active: Boolean)

  implicit val userSerializer = new TypeBuilder()
    .field[String]
    .field[String]
    .field[Int]
    .field[Boolean]
    .build(User.apply, User.unapply)
```
If you want to code your own functions, you can do this:
```Scala
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
```

As you can see, we are using implicits arguments for each **field** method invocation.

## Complex objects serializers.

In the sample above we declared *userSerializer* as an implicit value, we did it because we are going to create another serializer that uses *userSerializer*.

First we need define a new class.

```scala
  case class Sale(id: Long, cost: Double, owner: User)
```

And now...

```scala
val saleSerializer = new TypeBuilder()
	.field[Long]
	.field[Double]
	.field[User]
	.build(Sale.apply, Sale.unapply)
```

Every *zerializer* has the follow signature:

```scala

trait Zerializer[-I, +R] {

  def emptyValue: R

  def isEmpty(value: I): Boolean

  def read(input: DataInput): R

  def read(bytes: Array[Byte]): R

  def read(input: InputStream): R

  def write(value: I, output: DataOutput): Unit

  def write(value: I): Array[Byte]

  def write(value: I, output: OutputStream): Unit
}

```

We can use our serializer as the samples bellow:

```scala
	val user = User("Albert", "e@m.c2", 50, true)

	userSerializer.write(user, new FileOutputStream("user-01"))

	assert(userSerializer.read(new FileInputStream("user-01")) == user)

	val sale = Sale(599, 0.56, user)

	saleSerializer.write(sale, new FileOutputStream("sale-01"))

	assert(saleSerializer.read(new FileInputStream("sale-01")) == sale)

```

## How does it work?

`TypeBuilder` is a builder to create a `ComposedZerializer`, and this is a special `Zerializer` that has own binary format. The `ComposedZerializer` format has a header which is composed by a long64 field.

```c

ComposedFormatN {
	signed long64 control;

	byte[] field1;
	...
	byte[] fieldN;
}

```

## Control field

The control field describes how to deserialize the root object and which fields should or should not to be read. And it also have a reserved space to store the serialization version.

| 63 	| ... 	| 53 	| ... 	| 45 	| ... 	| 01 	| 00 	|
|:--:	|:---:	|:--:	|:---:	|:--:	|:---:	|:--:	|:--:	|
|    	|     	|    	|     	|    	|     	|    	|    	|
