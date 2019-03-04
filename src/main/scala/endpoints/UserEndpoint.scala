package endpoints

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import models._
import models.repository._
import models.repository.UserHandler._

import scala.concurrent.ExecutionContext
import akka.pattern.ask
import akka.util.Timeout
import org.json4s.{ DefaultFormats, native }

import scala.concurrent.duration._

class UserEndpoint(repository: UserRepository, userHandlerActor: ActorRef)(implicit ec:ExecutionContext, mat: Materializer) {

  implicit val timeout         = Timeout(5 seconds)
  import de.heikoseeberger.akkahttpjson4s.Json4sSupport._
  implicit val serialization   = native.Serialization
  implicit val formats         = DefaultFormats

  val userRoutes = {
    pathPrefix("api" / "accounts") {
      (get & path(Segment).as(FindByIdRequest)) { request =>

        val futureHttpResponse = userHandlerActor ? GetUser(request.id) flatMap  {
          case (statusCode: StatusCode,user:UserResource) =>
            Marshal(user).to[ResponseEntity].map {e => HttpResponse(entity = e, status = statusCode)}
          case (statusCode: StatusCode,msg:Message) =>
            Marshal(msg).to[ResponseEntity].map { e => HttpResponse(entity = e, status = statusCode)}
        }

        complete(futureHttpResponse)

      } ~ (post & pathEndOrSingleSlash & entity(as[UserResource])) { user =>

        val futureHttpResponse = userHandlerActor ? SaveUser(user.asDomain) flatMap {
          case (statusCode: StatusCodes.Success, msg:Message) =>
            Marshal(msg).to[ResponseEntity].map{ e => HttpResponse(entity = e, status = statusCode, headers = List(Location(s"/api/accounts/${msg.message}")))}
          case (statusCode: StatusCode, msg:Message) =>
            Marshal(msg).to[ResponseEntity].map{ e => HttpResponse(entity = e, status = statusCode)}
        }
        complete(futureHttpResponse)
      }
    }
  }

}