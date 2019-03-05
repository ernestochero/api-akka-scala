package models
import org.bson.types.ObjectId


case class FindByIdRequest(id: String) {
  require(ObjectId.isValid(id), "the informed id is not a representation of a valid hex string")
}

case class User(_id: ObjectId, username: String, age: Option[Int]) {
  def asResource = UserResource(_id.toHexString, username, age)
}

case class UserResource(id: String, username: String, age: Option[Int]) {
  require(username != null, "username not informed")
  require(username.nonEmpty, "username cannot be empty")

  def asDomain = User(if (id == null) ObjectId.get() else new ObjectId(id), username, age)
}

case class Message(message: String)

