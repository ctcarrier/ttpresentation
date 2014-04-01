package com.ttpresentation.model

import reactivemongo.bson.BSONObjectID

/**
 * Created by ctcarrier on 3/3/14.
 */
object User {
  val rx = "(^[\\w\\._%+-]+@[\\w\\.-]+\\.[\\w]{2,4}$)".r

  def validEmail(email: String) = {
    rx.findFirstIn(email) match {
      case Some(_) => true
      case None => false
    }
  }
}

case class User(_id: Option[BSONObjectID], email: String, password: String) {

  require (User.validEmail(email), "Invalid email")
  require (!password.isEmpty, "Password required")
}
