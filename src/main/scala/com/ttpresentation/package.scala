package com

import reactivemongo.bson.{BSONHandler, BSONDateTime, Macros}
import com.ttpresentation.model._
import com.github.nscala_time.time.Imports._
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by ccarrier for bl-rest.
 * at 3:19 PM on 12/15/13
 */
package object ttpresentation {

  DateTimeZone.setDefault(DateTimeZone.UTC)

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    val fmt = ISODateTimeFormat.dateTime()
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }

  implicit val userHandler = Macros.handler[User]
  implicit val vaultHandler = Macros.handler[Vault]
}
