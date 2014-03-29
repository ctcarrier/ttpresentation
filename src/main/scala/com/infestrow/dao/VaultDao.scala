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
import scala.util.{Failure, Success}
import reactivemongo.core.commands.{FindAndModify, Update}
import org.joda.time.DateTime

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
  def markAndReturn(vaultId: BSONObjectID, state: String): Future[Option[Vault]]
  def getAllVaultUserState(vaultId: BSONObjectID): Future[List[Option[String]]]
  def getUnlockedVaults(): Future[List[Vault]]

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

    stateCollection.update(selector = query, update = update, upsert = true).map(le => {
      if (le.ok){
        getAllVaultUserState(key).map(sl => {
          sl.collect({
            case None => None
            case Some(str) if !str.equals(Vault.CONFIRMED) => Some(str)
          })
        }).andThen({
          case Success(l: List[Option[String]]) if l.isEmpty => {
            logger.info("Setting vault %s to unlocked".format(key))
            setVaultState(key, Vault.UNLOCKED)
          }
          case _ => logger.error("Got some kind of error processing vault state in vault: ".format(key))
        })
      }
    })
  }

  def getVaultUserState(vaultId: BSONObjectID, email: String): Future[Option[String]] = {
    val query = BSONDocument("email" -> email, "vaultId" -> vaultId)

    stateCollection.find(query).one[BSONDocument].map(us => {
      us match {
        case Some(doc) => doc.getAs[String]("state")
        case _ => None
      }
    })
  }

  def getAllVaultUserState(vaultId: BSONObjectID): Future[List[Option[String]]] = {
    val query = BSONDocument("vaultId" -> vaultId)

    stateCollection.find(query).cursor[BSONDocument].collect[List]().map(x => x.map(y => y.getAs[String]("state")))
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

  def setVaultState(vaultId: BSONObjectID, state: String) {
    val query = BSONDocument("_id" -> vaultId)
    val update = BSONDocument("$set" -> BSONDocument("state" -> state))

    collection.update(selector = query, update = update, upsert = true)
  }

  def getUnlockedVaults(): Future[List[Vault]] = {
    val selector = BSONDocument(
      "state" -> Vault.UNLOCKED, "unlockDate" -> BSONDocument("$lt" -> DateTime.now()))
    collection.find(selector).cursor[Vault].collect[List]()

  }

  def markAndReturn(vaultId: BSONObjectID, state: String): Future[Option[Vault]] = {
    val selector = BSONDocument("_id" -> vaultId)
    val modifier = BSONDocument("$set" -> BSONDocument("state" -> state))

    val command = FindAndModify(
      collection.name,
      selector,
      Update(modifier, false))

    db.command(command).map(x => x.map(y => y.as[Vault]))


  }

}