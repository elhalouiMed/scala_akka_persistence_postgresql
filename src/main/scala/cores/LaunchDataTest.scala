package cores

import akka.actor._
import akka.persistence.jdbc.query.scaladsl.JdbcReadJournal
import akka.persistence.query.PersistenceQuery
import akka.stream.{ ActorMaterializer, Materializer }
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContext

object LaunchDataTest extends App {

  val configName = "dataTest-application.conf"
  lazy val configuration = ConfigFactory.load(configName)
  implicit val system: ActorSystem = ActorSystem("ClusterSystem", configuration)
  sys.addShutdownHook(system.terminate())
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: Materializer = ActorMaterializer()
  lazy val readJournal: JdbcReadJournal = PersistenceQuery(system).readJournalFor[JdbcReadJournal](JdbcReadJournal.Identifier)
  val dataTestReadModelDatabase = slick.jdbc.JdbcBackend.Database.forConfig("dataTest-read-model", system.settings.config)

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
    | this is a very simple example of akka stream over tcp socket and use akka persistence jdbc plugin (postgresql)
    | based on https://github.com/dnvriend/akka-persistence-jdbc.git repository
    |
    |
  """.stripMargin

  println(banner)
  new Receiver("localhost", 9182).run()
}
