package actors

import akka.actor.{ ActorLogging, SupervisorStrategy }
import akka.event.LoggingReceive
import akka.persistence.PersistentActor
import akka.stream.actor.{ ActorSubscriber, MaxInFlightRequestStrategy }
import scala.concurrent.duration._
object DataTest {

  case class FlightData(from: String, to: String, distanceMiles: Int, delayMinutes: Int)
  object FlightData {
    def apply(fields: Array[String]): Option[FlightData] = {
      try {
        Some(FlightData(fields(16), fields(17), fields(18).toInt, fields(14).toInt))
      } catch {
        case _: Exception => None
      }
    }
  }

  case class FlightWithDelayPerMile(flight: FlightData, delayPerMile: Double) extends Comparable[FlightWithDelayPerMile] {
    override def compareTo(o: FlightWithDelayPerMile) = delayPerMile.compareTo(o.delayPerMile)
  }
  object FlightWithDelayPerMile {
    def apply(data: FlightData): Option[FlightWithDelayPerMile] = {
      if (data.delayMinutes > 0) {
        Some(FlightWithDelayPerMile(data, 60.0d * data.delayMinutes / data.distanceMiles))
      } else None
    }
  }
}
class DataTest extends PersistentActor with ActorSubscriber with ActorLogging {
  import DataTest._

  private var inFlight = 0
  private var msgCounter = 0
  private var evntPersisted = 0
  private var numberToDisplat = 50
  var now: Long = System.currentTimeMillis()
  var oldCounter = 0
  override protected def requestStrategy = new MaxInFlightRequestStrategy(10) {
    override def inFlightInternally = inFlight
  }

  override val persistenceId: String = "persistence_actor"
  context.setReceiveTimeout(300.millis)
  override def receiveRecover: Receive = LoggingReceive {
    case default =>
      println(s"recover ${default}")
  }

  override def receiveCommand: Receive = LoggingReceive {
    // continue here
    case data: FlightData =>
      FlightWithDelayPerMile(data).foreach { d =>
        inFlight += 1
        msgCounter += 1
        if (msgCounter % numberToDisplat == 0) {
          println(s"${msgCounter} msg received where ${msgCounter - oldCounter} at ${System.currentTimeMillis() - now} ms")
          oldCounter = msgCounter
          now = System.currentTimeMillis()
          //println(s"${msgCounter} msg received")
        }
        //println(s"${msgCounter} msg received")
        persistAsync(d) { _ =>
          evntPersisted += 1
          if (evntPersisted % numberToDisplat == 0) { println(s"${evntPersisted} msg persisted") }
          //println(s"data to persist => ${d}")
          inFlight -= 1
        }
      }
    case SupervisorStrategy.Stop ⇒ context.stop(self)
  }

}
