package com.infestrow.model

import reactivemongo.bson.BSONObjectID

/**
 * Created by ctcarrier on 3/4/14.
 */
case class Invite(_id: Option[BSONObjectID], email: String, vaultId: Option[BSONObjectID], userId: Option[BSONObjectID])
