package com

import reactivemongo.bson.{BSONHandler, BSONDateTime, Macros}
import com.infestrow.model._
import com.github.nscala_time.time.Imports._
import org.joda.time.format.ISODateTimeFormat

/**
 * Created by ccarrier for bl-rest.
 * at 3:19 PM on 12/15/13
 */
package object infestrow {

  DateTimeZone.setDefault(DateTimeZone.UTC)

  implicit object BSONDateTimeHandler extends BSONHandler[BSONDateTime, DateTime] {
    val fmt = ISODateTimeFormat.dateTime()
    def read(time: BSONDateTime) = new DateTime(time.value)
    def write(jdtime: DateTime) = BSONDateTime(jdtime.getMillis)
  }

  implicit val vaultUserHandler = Macros.handler[VaultUser]
  implicit val vaultAccessHandler = Macros.handler[VaultAccess]
  implicit val vaultHandler = Macros.handler[Vault]
  implicit val dataHandler = Macros.handler[VaultData]
  implicit val userHandler = Macros.handler[User]
  implicit val inviteHandler = Macros.handler[Invite]
}
