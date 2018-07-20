// data not comming from sender

package cores
import actors.DataTest
import actors.DataTest.FlightData
import akka.actor.{ ActorSystem, Props }
import akka.stream.{ ActorMaterializer, ThrottleMode }
import akka.stream.scaladsl.{ Flow, Sink, Source, Tcp }
import akka.stream.scaladsl.Tcp.{ IncomingConnection, ServerBinding }
import akka.util.ByteString
import akka.stream.scaladsl.Framing

import scala.concurrent.Future
import scala.concurrent.duration._

class Receiver(host: String, port: Int)(implicit val system: ActorSystem) {

  def toInt(s: String): Int = {
    try {
      s.toInt
    } catch {
      case e: Exception => 0
    }
  }

  def run(): Unit = {
    implicit val materializer = ActorMaterializer()
    val dataTestActor = system.actorOf(Props[DataTest])
    val connections: Source[IncomingConnection, Future[ServerBinding]] =
      Tcp().bind(host, port)

    connections runForeach { connection ⇒
      println(s"New connection from: ${connection.remoteAddress}")

      val echo: Flow[ByteString, ByteString, _] = Flow[ByteString]
        .via(Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 400000000,
          allowTruncation = true
        ))
        .throttle(50, 5.second, 50, ThrottleMode.shaping)
        .map(_.utf8String)
        .filter(_.startsWith("20"))
        .map(x => {
          //println(s"received ${x}")
          val l = x.split(",")
          dataTestActor ! new FlightData(l(16), l(17), toInt(l(18)), toInt(l(14)))
          //Thread.sleep(10)
          ByteString(x)
        }).recover {
          case x: RuntimeException => {
            println(s"\n\n\n Exception ${x} \n\n\n")
            ByteString("")
          }
        }

      //connection.handleWith()
      connection.handleWith(echo)

    }

  }
}
