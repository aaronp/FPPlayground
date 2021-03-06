package uk.co.odinconsultants.fp.cats.errors

import cats.effect.{IO, IOApp}
import cats.effect.{ExitCode, IO, IOApp, Timer}
import scala.concurrent.duration._
import cats.implicits._

object MyRethrowMain extends IOApp {

  case class Table(x: String)

  val trigger = "blow up!"

  def loadTable(table: Table): IO[Table] = IO {
    if (table.x == trigger)
      throw new Exception(trigger)
    else
      Table(s"loaded ${table.x}")
  }

  override def run(args: List[String]): IO[ExitCode] = {
    val tableList = List(Table("1"), Table(trigger), Table("2"), Table(trigger), Table("3)"))

    val tapFunction: Either[Throwable, Table] => IO[Unit] = _ match {
      case Left(x)  => IO(println(s"Blew up on tapping with $x"))
      case Right(x) => IO(println(s"tapped $x"))
    }

    val io: IO[Unit] = tableList.parTraverse_ { t =>
      loadTable(t).attempt.flatTap(tapFunction).rethrow // rethrow actually blows up the App!
    }
    // Gavin Bisesi @Daenyth Jun 05 18:06 (typelevel/cats-effect Gitter)
    //  ^ simple and straightforward
    //    the attempt/rethrow thing is almost .guaranteeCase except that guarantee lets you handle "cancelled" too
    //  whereas attempt/rethrow means that cancel also cancels the flatTap part
    io.as(ExitCode.Success)
  }
}
