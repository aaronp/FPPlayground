package uk.co.odinconsultants.fp.zio.streams

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, IOException}

import zio.ZIO
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test.environment._
import zio.test.{DefaultRunnableSpec, ZSpec, suite, testM, _}

import zio.test.TestAspect._
import zio.console._
import zio.duration._


object PipeMainSpec extends DefaultRunnableSpec {

  import PipeMain._

  val msg: String = "This is a test" * 10000
  val bytes: Array[Byte] = msg.getBytes()

  override def spec: ZSpec[TestEnvironment, Any] = {
    suite("Pipe")(
      testM("should be non blocking"){
          val inStream  = new ByteArrayInputStream(bytes)
          val outStream = new ByteArrayOutputStream(bufferSize)
          val result: ZIO[Blocking, IOException, TestResult] = for {
            // fork/join or no fork/join makes no difference here as doPipe is synchronous
            p <- doPipe(inStream, outStream) //.fork
//            _ <- p.join
          } yield {
            assert(new String(outStream.toByteArray))(equalTo(msg))
          }
          result
        }
      ,
      testM("should read slow, blocking streams"){
        for {
          _ <- TestClock.adjust(15.seconds)
          s <- finiteSlowStream.chunkN(1024).runCollect
        } yield {
          assert(s.mkString(","))(equalTo(Array(0,1,2,3,4,5).map(_.toByte).mkString(",")))
        }
      } @@ timeout(10 seconds)
    )
  }
}
