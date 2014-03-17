package com.infestrow.dao

import scala.concurrent.Future
import akka.actor.ActorSystem

import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.api.{DB}
import reactivemongo.api.collections.default._

import com.infestrow._

import com.typesafe.scalalogging.slf4j.Logging

import com.infestrow.model._
import com.infestrow.model.VaultData
import scala.Some
import reactivemongo.api.collections.default.BSONCollection
import scala.util.Success

/**
 * Created by ccarrier for bl-rest.
 * at 10:00 PM on 12/14/13
 */
trait VaultDao {

  def get(key: BSONObjectID, user: User): Future[Option[Vault]]
  def save(v: Vault, user: User): Future[Option[Vault]]
  def getAll(user: User): Future[List[Vault]]
  def save(vd: VaultData, user: User): Future[Option[VaultData]]
  def addUser(key: BSONObjectID, vaultUser: VaultUser): Future[Option[Vault]]
  def getVaultData(key: BSONObjectID, user: User): Future[Option[VaultData]]
  def getVaultUserState(vaultId: BSONObjectID, email: String): Future[Option[String]]
  def getVaultState(key: BSONObjectID): Future[List[VaultUser]]

}

class VaultReactiveDao(db: DB, collection: BSONCollection, dataCollection: BSONCollection, stateCollection: BSONCollection, system: ActorSystem) extends VaultDao with Logging {

  implicit val context = system.dispatcher

  def get(key: BSONObjectID, user: User): Future[Option[Vault]] = {
    collection.find(BSONDocument("_id" -> key, "access.allowedUsers.email" -> BSONDocument("$in" -> List(user.email)))).one[Vault]
  }

  def getAll(user: User): Future[List[Vault]] = {
    val query = BSONDocument("_id" -> BSONDocument("$exists" -> true), "access.allowedUsers.email" -> BSONDocument("$in" -> List(user.email)))
    collection.find(query).cursor[Vault].collect[List]()
  }

  def save(v: Vault, user: User): Future[Option[Vault]] = {
    val toSave = v.copy(_id = Some(BSONObjectID.generate), access = Some(VaultAccess(user._id.get, List(user.email))))
    collection.save(toSave).map(x => {
      setVaultUserState(toSave._id.get, user.email, Vault.UNCONFIRMED)
      Some(toSave)
    })
  }

  def addUser(key: BSONObjectID, vaultUser: VaultUser): Future[Option[Vault]] = {
    val query = BSONDocument("_id" -> key)
    val update = BSONDocument("$addToSet" -> BSONDocument("access.allowedUsers" -> vaultUser))

    collection.update(query, update).flatMap(x => {
      collection.find(BSONDocument("_id" -> key, "access.allowedUsers.email" -> BSONDocument("$in" -> List(vaultUser.email)))).one[Vault].andThen({
        case Success(v) => setVaultUserState(key, vaultUser.email, Vault.UNCONFIRMED)
      })
    })
  }

  def getVaultState(key: BSONObjectID): Future[List[VaultUser]] = {
    val query = BSONDocument("vaultId" -> key)

    collection.find(query).cursor[VaultUser].collect[List]()
  }

  private def setVaultUserState(key: BSONObjectID, email: String, state: String){
    val query = BSONDocument("email" -> email, "vaultId" -> key)
    val update = BSONDocument("$set" -> BSONDocument("state" -> state))

    collection.update(selector = query, update = update, upsert = true)
  }

  def getVaultUserState(vaultId: BSONObjectID, email: String): Future[Option[String]] = {
    val query = BSONDocument("email" -> email, "vaultId" -> vaultId)

    collection.find(query).one[BSONDocument].map(us => {
      us match {
        case Some(doc) => doc.getAs[String]("state")
        case _ => None
      }
    })
  }

  def save(vd: VaultData, user: User): Future[Option[VaultData]] = {
    val toSave = vd.copy(_id = Some(BSONObjectID.generate))
    val result = dataCollection.save(toSave).map(x => {Some(toSave)})
    setVaultUserState(vd.vaultId.get, user.email, Vault.CONFIRMED)
    result
  }

  def getVaultData(key: BSONObjectID, user: User): Future[Option[VaultData]] = {
    logger.info("Getting VD with %s and %s".format(key.stringify, user.email))
    dataCollection.find(BSONDocument("vaultId" -> key, "userId" -> user._id)).one[VaultData]
  }
}