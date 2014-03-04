package com.infestrow.dao

import scala.concurrent.Future
import akka.actor.ActorSystem

import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.api.{DB}
import reactivemongo.api.collections.default._

import com.infestrow._

import com.typesafe.scalalogging.slf4j.Logging

import com.infestrow.model.Vault

/**
 * Created by ccarrier for bl-rest.
 * at 10:00 PM on 12/14/13
 */
trait VaultDao {

  def get(key: BSONObjectID): Future[Option[Vault]]
  def save(v: Vault): Future[Option[Vault]]
  def getAll: Future[List[Vault]]

}

class VaultReactiveDao(db: DB, collection: BSONCollection, system: ActorSystem) extends VaultDao with Logging {

  implicit val context = system.dispatcher

  def get(key: BSONObjectID): Future[Option[Vault]] = {
    logger.info("Getting vault: %s".format(key))
    collection.find(BSONDocument("_id" -> key)).one[Vault]
  }

  def getAll: Future[List[Vault]] = {
    val query = BSONDocument("_id" -> BSONDocument("$exists" -> true))
    collection.find(query).cursor[Vault].collect[List]()
  }

  def save(v: Vault): Future[Option[Vault]] = {
    val toSave = v.copy(_id = Some(BSONObjectID.generate))
    collection.save(toSave).map(x => {Some(toSave)})

  }
}