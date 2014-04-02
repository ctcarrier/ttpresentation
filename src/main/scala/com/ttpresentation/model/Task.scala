package com.ttpresentation.model

import reactivemongo.bson.{BSONObjectID, BSONDocumentReader, BSONDocument}
import com.github.nscala_time.time.Imports._

/**
 * Created by ccarrier for bl-rest.
 * at 2:53 PM on 12/15/13
 */

case class Task(_id: Option[BSONObjectID], name: String, userId: Option[BSONObjectID])

