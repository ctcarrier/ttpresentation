package com.infestrow.json

import org.json4s.jackson.Serialization
import org.json4s._
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import org.json4s.ShortTypeHints

/**
 * Created by ccarrier for bl-rest.
 * at 9:58 PM on 12/14/13
 */
trait LocalJacksonFormats {

  implicit val json4sJacksonFormats = Serialization.formats(ShortTypeHints(List(classOf[BSONDocument]))) + new IntervalSerializer

}

class IntervalSerializer extends CustomSerializer[BSONObjectID](format => (
  {
    case JString(id) =>
      BSONObjectID(id)
  },
  {
    case x: BSONObjectID =>
      JString(x.stringify)
  }
  ))