package cores

import actors.{ Person, SupportDesk }
import akka.actor._
import akka.cluster.sharding.{ ClusterSharding, ClusterShardingSettings }
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object LaunchPerson extends App {
  val configName = "person-application.conf"
  lazy val configuration = ConfigFactory.load(configName)
  implicit val system: ActorSystem = ActorSystem("ClusterSystem", configuration)
  sys.addShutdownHook(system.terminate())
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()
  lazy val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)

  val personRegion: ActorRef = ClusterSharding(system).start(
    typeName = Person.PersonShardName,
    entityProps = Props[Person],
    settings = ClusterShardingSettings(system),
    extractEntityId = Person.extractEntityId,
    extractShardId = Person.extractShardId
  )

  val supportDesk = system.actorOf(Props(new SupportDesk(personRegion, readJournal)))
  val personReadModelDatabase = slick.jdbc.JdbcBackend.Database.forConfig("person-read-model", system.settings.config)

  val banner = s"""
    |
    |#####  ###### #    #  ####
    |#    # #      ##  ## #    #
    |#    # #####  # ## # #    #
    |#    # #      #    # #    #
    |#    # #      #    # #    #
    |#####  ###### #    #  ####
    |
    | Developped by ELHALOUI Mohammed
    | this is a very simple example of akka persistence jdbc plugin using postgresql
    | based on https://github.com/dnvriend/akka-persistence-jdbc.git repository
    |
    |
  """.stripMargin

  println(banner)
}
