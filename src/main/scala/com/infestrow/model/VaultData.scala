package com.infestrow.model

import reactivemongo.bson.BSONObjectID

/**
 * Created by ctcarrier on 3/5/14.
 */
case class VaultData(_id: Option[BSONObjectID], data: String, vaultId: Option[BSONObjectID], userId: Option[BSONObjectID])
