package actors

import akka.actor.{ ActorLogging, SupervisorStrategy }
import akka.cluster.sharding.ShardRegion
import akka.event.LoggingReceive
import akka.persistence.PersistentActor

import scala.concurrent.duration._

object Person {

  sealed trait Command
  final case class CreatePerson(firstName: String, lastName: String, timestamp: Long) extends Command
  // events
  sealed trait Event
  final case class PersonCreated(firstName: String, lastName: String, timestamp: Long) extends Event
  // the state
  final case class PersonState(firstName: String = "", lastName: String = "")

  // necessary for cluster sharding
  final case class EntityEnvelope(id: String, payload: Any)

  final val NumberOfShards: Int = 100

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case EntityEnvelope(id, payload) ⇒ (id.toString, payload)
  }

  val extractShardId: ShardRegion.ExtractShardId = {
    case EntityEnvelope(id, _) ⇒ (id.hashCode % NumberOfShards).toString
  }

  final val PersonShardName = "Person"
}
class Person extends PersistentActor with ActorLogging {
  import Person._

  override val persistenceId: String = "Person-" + self.path.name

  context.setReceiveTimeout(300.millis)

  var state = PersonState()

  def handleEvent(event: Event): Unit = event match {
    case PersonCreated(firstName, lastName, _) ⇒ state = state.copy(firstName = firstName, lastName = lastName)

  }

  override def receiveRecover: Receive = LoggingReceive {
    case event: Event ⇒ {
      println(s"Recover event ${event}")
      handleEvent(event)
    }
  }

  def now: Long = System.currentTimeMillis()

  override def receiveCommand: Receive = LoggingReceive {
    case CreatePerson(firstName, lastName, _) ⇒ {
      println(s"Receive command CreatePerson(${firstName},${lastName}) ")
      persist(PersonCreated(firstName, lastName, now))(handleEvent)
    }
    case SupervisorStrategy.Stop ⇒ context.stop(self)
  }

}
