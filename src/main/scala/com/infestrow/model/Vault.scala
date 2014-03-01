package com.infestrow.model

import reactivemongo.bson.{BSONObjectID, BSONDocumentReader, BSONDocument}

/**
 * Created by ccarrier for bl-rest.
 * at 2:53 PM on 12/15/13
 */
case class Vault(
                      _id: Option[BSONObjectID],
                      name: String
                      )

