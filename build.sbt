name := "scala-akka-persistence-postgresql"

organization := "net.innovlab.elhaloui"

version := "1.0.0"

scalaVersion := "2.11.8"

developers := List(
  Developer("@dnvriend", "Dennis Vriend", "", url("https://nl.linkedin.com/in/dnvriend")),
  Developer("@ELHalouiMed", "ELHALOUI Mohammed", "mohammed.elhaloui.se@gmail.com", url("https://www.linkedin.com/in/mohammed-elhaloui-822a4a95/"))
)
// the akka-persistence-jdbc plugin lives here
resolvers += Resolver.jcenterRepo

// the slick-extension library (which is used by akka-persistence-jdbc) lives here
resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/maven-releases/"

libraryDependencies ++= {
  val akka = "2.4.7"
  val akkaPersJdbcV = "2.4.0"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akka,
    "com.typesafe.akka" %% "akka-stream" % akka,
    "com.typesafe.akka" %% "akka-slf4j" % akka,
    "com.typesafe.akka" %% "akka-persistence" % akka,
    "com.typesafe.akka" %% "akka-persistence-query-experimental" % akka,
    "com.typesafe.akka" %% "akka-cluster" % akka,
    "com.typesafe.akka" %% "akka-cluster-sharding" % akka,
    "com.typesafe.akka" %% "akka-stream-testkit" % akka % Test,
    "org.postgresql" % "postgresql" % "9.4.1208",
    "com.lihaoyi" %% "pprint" % "0.4.1",
    "org.scalaz" %% "scalaz-core" % "7.2.3",
    "com.twitter" %% "chill-akka" % "0.8.0",
    "com.github.dnvriend" %% "akka-persistence-jdbc" % akkaPersJdbcV changing(),
    "ch.qos.logback" % "logback-classic" % "1.1.7",
    "org.scalatest" %% "scalatest" % "2.2.6" % Test
  )
}
fork in Test := true
