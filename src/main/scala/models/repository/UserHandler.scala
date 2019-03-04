package models.repository

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import models._

import scala.util.{Failure, Success}
object UserHandler {

  def props(repository: UserRepository): Props = Props(new UserHandler (repository))

  case class GetUser(id: String)
  case class SaveUser(user: User)

}

class UserHandler(repository: UserRepository) extends Actor with ActorLogging {
  import UserHandler._
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case GetUser(id) =>
      val _sender = sender()
      repository.findById(id).onComplete {
        case Success(Some(user)) => _sender ! (StatusCodes.Found, user)
        case Success(None) => _sender ! (StatusCodes.NotFound,Message("User Not Found"))
        case Failure(e) => _sender ! (StatusCodes.InternalServerError,Message(e.getMessage))
      }

    case SaveUser(user:User) =>
      val _sender = sender()
      repository.saveOpt(user).onComplete {
        case Success(Some(id)) => _sender ! (StatusCodes.Created, Message(id))
        case Failure(e) => _sender ! (StatusCodes.InternalServerError, Message(e.getMessage))
      }
  }
}