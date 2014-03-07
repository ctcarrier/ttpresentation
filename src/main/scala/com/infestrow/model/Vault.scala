package com.infestrow.model

import reactivemongo.bson.{BSONObjectID, BSONDocumentReader, BSONDocument}

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
}

case class Vault(
                      _id: Option[BSONObjectID],
                      name: String, userId: Option[BSONObjectID], state: String = Vault.UNCONFIRMED
                      )

