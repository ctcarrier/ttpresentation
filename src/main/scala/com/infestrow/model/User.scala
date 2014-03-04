package com.infestrow.model

import reactivemongo.bson.BSONObjectID

/**
 * Created by ctcarrier on 3/3/14.
 */
case class User(_id: Option[BSONObjectID], email: String, password: String)
