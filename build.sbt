name := "projectAPI"

version := "0.1"

scalaVersion := "2.12.6"

val akkaHttp = "10.1.1"
val akka = "2.5.11"
val circe = "0.9.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttp,
  "com.typesafe.akka" %% "akka-stream" % akka,
  "com.typesafe.akka" %% "akka-slf4j" % akka,

  "de.heikoseeberger" %% "akka-http-json4s" % "1.25.2",
  "org.reactivemongo" %% "reactivemongo" % "0.16.0",

  "org.json4s" %% "json4s-native" % "3.6.0",

  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.1.0",
)

