package com.infestrow.dao

import scala.concurrent.Future
import akka.actor.ActorSystem

import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.api.{DB}
import reactivemongo.api.collections.default._

import com.infestrow._

import com.typesafe.scalalogging.slf4j.Logging

import com.infestrow.model.{User, Vault}

/**
 * Created by ccarrier for bl-rest.
 * at 10:00 PM on 12/14/13
 */
trait VaultDao {

  def get(key: BSONObjectID, user: User): Future[Option[Vault]]
  def save(v: Vault): Future[Option[Vault]]
  def getAll(user: User): Future[List[Vault]]

}

class VaultReactiveDao(db: DB, collection: BSONCollection, system: ActorSystem) extends VaultDao with Logging {

  implicit val context = system.dispatcher

  def get(key: BSONObjectID, user: User): Future[Option[Vault]] = {
    logger.info("Getting vault: %s".format(key))
    collection.find(BSONDocument("_id" -> key, "userId" -> user._id)).one[Vault]
  }

  def getAll(user: User): Future[List[Vault]] = {
    val query = BSONDocument("_id" -> BSONDocument("$exists" -> true), "userId" -> user._id)
    collection.find(query).cursor[Vault].collect[List]()
  }

  def save(v: Vault): Future[Option[Vault]] = {
    val toSave = v.copy(_id = Some(BSONObjectID.generate))
    collection.save(toSave).map(x => {Some(toSave)})

  }
}