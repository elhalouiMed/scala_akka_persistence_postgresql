package actors

import akka.actor.{ ActorLogging, SupervisorStrategy }
import akka.event.LoggingReceive
import akka.persistence.PersistentActor
import akka.stream.actor.{ ActorSubscriber, MaxInFlightRequestStrategy }

import scala.concurrent.duration._

object Person {

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
class Person extends PersistentActor with ActorSubscriber with ActorLogging {
  import Person._

  private var inFlight = 0
  override protected def requestStrategy = new MaxInFlightRequestStrategy(10) {
    override def inFlightInternally = inFlight
  }

  override val persistenceId: String = "Persistence_postgre_actor"

  context.setReceiveTimeout(300.millis)

  //var state = PersonState()

  override def receiveRecover: Receive = LoggingReceive {
    case default =>
      println(s"recover ${default}")
  }

  def now: Long = System.currentTimeMillis()

  override def receiveCommand: Receive = LoggingReceive {
    // continue here
    case data: FlightData =>
      FlightWithDelayPerMile(data).foreach { d =>
        inFlight += 1
        persistAsync(d) { _ =>
          println(s"data to persist => ${d}")
          inFlight -= 1
        }
      }
    case SupervisorStrategy.Stop ⇒ context.stop(self)
  }

}
