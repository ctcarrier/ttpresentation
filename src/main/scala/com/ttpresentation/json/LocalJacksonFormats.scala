package com.ttpresentation.json

import org.json4s.jackson.Serialization
import org.json4s._
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import org.json4s.ShortTypeHints
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by ccarrier for bl-rest.
 * at 9:58 PM on 12/14/13
 */
trait LocalJacksonFormats {

  implicit val json4sJacksonFormats = Serialization.formats(ShortTypeHints(List(classOf[BSONDocument]))) +
    new BSONObjectIDSerializer + new DateTimeSerializer

}

class BSONObjectIDSerializer   extends CustomSerializer[BSONObjectID](format => (
  {
    case JString(id) =>
      BSONObjectID(id)
  },
  {
    case x: BSONObjectID =>
      JString(x.stringify)
  }
  ))

class DateTimeSerializer   extends CustomSerializer[DateTime](format => (
  {
    case JString(id) =>
      val fmt = ISODateTimeFormat.dateTime()
      fmt.parseDateTime(id)
  },
  {
    case x: DateTime =>
      val fmt = ISODateTimeFormat.dateTime()
      JString(fmt.print(x))
  }
  ))