// data not comming from sender

package cores
import actors.Person
import actors.Person.FlightData
import akka.actor.{ ActorSystem, Props }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ Flow, Sink, Source, Tcp }
import akka.stream.scaladsl.Tcp.{ IncomingConnection, ServerBinding }
import akka.util.ByteString
import akka.stream.scaladsl.Framing
import scala.concurrent.Future

class Receiver(host: String, port: Int)(implicit val system: ActorSystem) {
  def run(): Unit = {
    implicit val mat = ActorMaterializer()
    val personActor = system.actorOf(Props[Person])
    val connections: Source[IncomingConnection, Future[ServerBinding]] =
      Tcp().bind(host, port)
    connections runForeach { connection ⇒
      println(s"New connection from: ${connection.remoteAddress}")

      val echo = Flow[ByteString]
        .via(Framing.delimiter(
          ByteString("\n"),
          maximumFrameLength = 400000,
          allowTruncation = true
        ))
        .map(_.utf8String)
        .filter(_.startsWith("20"))
        .map(x => {
          println(s"received ${x}")
          val l = x.split(",")
          personActor ! new FlightData(l(16), l(17), l(18).toInt, l(14).toInt)
          Thread.sleep(1000)
          ByteString(x)
        })
      connection.handleWith(echo)
    }

  }
}
