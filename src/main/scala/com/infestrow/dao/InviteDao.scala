package com.infestrow.dao

import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.Future
import com.infestrow.model.{User, Invite}
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import akka.actor.ActorSystem
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Created by ctcarrier on 3/4/14.
 */
trait InviteDao {

  def get(key: BSONObjectID, user: User): Future[Option[Invite]]
  def save(v: Invite): Future[Option[Invite]]
  def getAll(vaultId: BSONObjectID, user: User): Future[List[Invite]]

}

class InviteReactiveDao(db: DB, collection: BSONCollection, system: ActorSystem) extends InviteDao with Logging {

  implicit val context = system.dispatcher

  def get(key: BSONObjectID, user: User): Future[Option[Invite]] = {
    logger.info("Getting invite: %s".format(key))
    collection.find(BSONDocument("_id" -> key, "userId" -> user._id)).one[Invite]
  }

  def getAll(vaultId: BSONObjectID, user: User): Future[List[Invite]] = {
    val query = BSONDocument("_id" -> BSONDocument("$exists" -> true), "vaultId" -> vaultId, "userId" -> user._id)
    collection.find(query).cursor[Invite].collect[List]()
  }

  def save(v: Invite): Future[Option[Invite]] = {
    val toSave = v.copy(_id = Some(BSONObjectID.generate))
    collection.save(toSave).map(x => {Some(toSave)})

  }
}
