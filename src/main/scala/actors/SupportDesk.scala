package actors

import java.util.UUID

import akka.actor.{ Actor, ActorLogging, ActorRef }
import akka.persistence.query.scaladsl.{ CurrentPersistenceIdsQuery, ReadJournal }
import akka.stream.Materializer
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class SupportDesk(personRegion: ActorRef, readJournal: ReadJournal with CurrentPersistenceIdsQuery)(implicit val mat: Materializer, ec: ExecutionContext) extends Actor with ActorLogging {
  import actors.Person._

  context.system.scheduler.schedule(1.second, 1.second, self, "GO")

  def now: Long = System.currentTimeMillis()

  override def receive: Receive = {
    case _ ⇒
      val id = UUID.randomUUID.toString
      personRegion ! EntityEnvelope(id, CreatePerson("FOO", "BAR", now))

  }
}
