package com.infestrow.model

import reactivemongo.bson.{BSONObjectID, BSONDocumentReader, BSONDocument}
import com.github.nscala_time.time.Imports._

/**
 * Created by ccarrier for bl-rest.
 * at 2:53 PM on 12/15/13
 */
trait VaultState{
  val name: String
}

object Vault {
  val CONFIRMED = "confirmed";
  val UNCONFIRMED = "unconfirmed"
  val UNLOCKED = "unlocked"
}

object VaultUser {
  implicit def stringToVault(str: String) = VaultUser(str)
}
case class VaultUser(email: String, state: String = Vault.UNCONFIRMED)
case class VaultAccess(owner: BSONObjectID, allowedUsers: List[VaultUser] = List.empty)

case class Vault(
                      _id: Option[BSONObjectID],
                      name: String, access: Option[VaultAccess], state: String = Vault.UNCONFIRMED, createdDate: Option[DateTime] = Some(DateTime.now), unlockDate: Option[DateTime]
                      )

