package uk.co.odinconsultants.fp.zio.modules

import izumi.reflect.Tags.Tag
import org.scalatest.{Matchers, WordSpec}
import zio.{Has, IO, ZIO}

class MyTypeInference extends WordSpec with Matchers {

  import MyTypeInference._

  type UserRepo = MyDesugaredHas[UserRepo.Service]

  object UserRepo {
    trait Service {
      def getUser(userId: UserId): IO[DBError, Option[User]]
    }
  }

  def myGetUser(userId: UserId): MyRead[UserRepo, DBError, Option[User]] = {
    MyTypeInference.accessM(_.get.getUser(userId))
  }

  "Argument" should {
    "be inferred in my code" in {
      println(myGetUser(UserId(1)))
    }
  }

}
final class MyDesugaredHas[A]

object MyDesugaredHas {
  type Tagged[A] = Tag[A]
  implicit final class MyDesugaredHasSyntax[Self <: MyDesugaredHas[_]](private val self: Self) extends AnyVal {
    def get[B](implicit ev: Self <:< MyDesugaredHas[_ <: B], tagged: Tagged[B]): B = ???
  }
}

object MyTypeInference {

  final class MyRead[R, E, A](val k: R => ZIO[R, E, A]) {
    def tag = 13
  }

  def accessM[R]: MyAccessMPartiallyApplied[R] =
    new MyAccessMPartiallyApplied[R]

  final class MyAccessMPartiallyApplied[R](private val dummy: Boolean = true) extends AnyVal {
    def apply[E, A](f: R => ZIO[R, E, A]): MyRead[R, E, A] =
      new MyRead(f)
  }

}
