package endpoints

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import models._
import models.repository._
import models.repository.UserHandler._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

class UserEndpoint(repository: UserRepository, userHandlerActor: ActorRef)(implicit ec:ExecutionContext, mat: Materializer) {

  implicit val timeout = Timeout(5 seconds)

  val userRoutes = {
    pathPrefix("api" / "users") {
      (get & path(Segment).as(FindByIdRequest)) { request =>
        onComplete(repository.findById(request.id)) {
          case Success(Some(user)) =>
            complete(Marshal(user).to[ResponseEntity].map {e => HttpResponse(entity = e)})
          case Success(None) =>
            complete(HttpResponse(status = StatusCodes.NotFound))
          case Failure(e) =>
            complete(Marshal(Message(e.getMessage)).to[ResponseEntity].map { e => HttpResponse(entity = e, status = StatusCodes.InternalServerError) })
        }
      } ~ (post & pathEndOrSingleSlash & entity(as[User])) { user =>
        onComplete(repository.save(user)) {
          case Success(id) =>
            complete(HttpResponse(status = StatusCodes.Created, headers = List(Location(s"/api/users/$id"))))
          case Failure(e) =>
            complete(Marshal(Message(e.getMessage)).to[ResponseEntity].map{ e => HttpResponse(entity = e, status = StatusCodes.InternalServerError)})
        }
      }
    }
  }

  val userRoutes2 = {
    pathPrefix("api" / "users") {
      (get & path(Segment).as(FindByIdRequest)) { request =>

        val futureHttpResponse = userHandlerActor ? GetUser(request.id) flatMap  {
          case (statusCode: StatusCode,user:User) =>
            Marshal(user).to[ResponseEntity].map {e => HttpResponse(entity = e, status = statusCode)}
          case (statusCode: StatusCode,msg:Message) =>
            Marshal(msg).to[ResponseEntity].map { e => HttpResponse(entity = e, status = statusCode)}
        }

        complete(futureHttpResponse)

      } ~ (post & pathEndOrSingleSlash & entity(as[User])) { user =>

        val futureHttpResponse = userHandlerActor ? SaveUser(user) flatMap {
          case (statusCode: StatusCodes.Success, msg:Message) =>
            Marshal(msg).to[ResponseEntity].map{ e => HttpResponse(entity = e, status = statusCode, headers = List(Location(s"/api/users/${msg.message}")))}
          case (statusCode: StatusCode, msg:Message) =>
            Marshal(msg).to[ResponseEntity].map{ e => HttpResponse(entity = e, status = statusCode)}
        }
        complete(futureHttpResponse)

      }
    }
  }

}