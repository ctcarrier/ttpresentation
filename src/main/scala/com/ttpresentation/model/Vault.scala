package com.ttpresentation.model

import reactivemongo.bson.{BSONObjectID, BSONDocumentReader, BSONDocument}
import com.github.nscala_time.time.Imports._

/**
 * Created by ccarrier for bl-rest.
 * at 2:53 PM on 12/15/13
 */

case class Vault(_id: Option[BSONObjectID], name: String, userId: Option[BSONObjectID], createdDate: Option[DateTime] = Some(DateTime.now))

