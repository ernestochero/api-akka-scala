import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import endpoints._
import models.repository.{UserHandler, UserRepository}
import mongodb.Mongo


object Application extends App{
  implicit val system: ActorSystem = ActorSystem("eralche-project-api")
  implicit val mat: ActorMaterializer = ActorMaterializer()
  implicit val ec:ExecutionContext = system.dispatcher


  val log = system.log
  val repository = new UserRepository(Mongo.userCollection)
  val userHandlerActor = system.actorOf(UserHandler.props(repository))

  val routes = new UserEndpoint(repository, userHandlerActor).userRoutes2

  Http().bindAndHandle(routes, "0.0.0.0", 8090).onComplete {
    case Success(b) => log.info(s"application is up and running at ${b.localAddress.getHostName}:${b.localAddress.getPort}")
    case Failure(e) => log.error(s"could not start application: {}", e.getMessage)
  }
}
