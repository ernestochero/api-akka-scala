package models
import org.bson.types.ObjectId


case class FindByIdRequest(id: String) {
  require(ObjectId.isValid(id), "the informed id is not a representation of a valid hex string")
}

case class User(_id: ObjectId,
                role: Option[String] = None,
                email: String,
                firstName: Option[String] = None,
                lastName: Option[String] = None,
                password: String,
                avatar_path: Option[String] = None,
                update_date: Option[String] = None
               ) {
  def asResource = UserResource(_id.toHexString, role, email, firstName, lastName, password, avatar_path, update_date)
}

case class UserResource(id: String,
                        role: Option[String] = None,
                        email: String,
                        firstName: Option[String] = None,
                        lastName: Option[String] = None,
                        password: String,
                        avatar_path: Option[String] = None,
                        update_date: Option[String] = None) {

  def asDomain = User(if (id == null) ObjectId.get() else new ObjectId(id), role, email, firstName, lastName, password, avatar_path, update_date)
}

case class Message(message: String)

