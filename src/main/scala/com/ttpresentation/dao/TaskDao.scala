package com.ttpresentation.dao

import scala.concurrent.Future
import akka.actor.ActorSystem

import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.api.{DB}
import reactivemongo.api.collections.default._

import com.ttpresentation._

import com.typesafe.scalalogging.slf4j.Logging

import com.ttpresentation.model._
import scala.Some
import reactivemongo.api.collections.default.BSONCollection
import scala.util.{Failure, Success}
import reactivemongo.core.commands.{FindAndModify, Update}
import org.joda.time.DateTime

/**
 * Created by ccarrier for bl-rest.
 * at 10:00 PM on 12/14/13
 */
trait TaskDao {

  def get(key: BSONObjectID, user: User): Future[Option[Task]]
  def save(v: Task, user: User): Future[Option[Task]]
  def getAll(user: User): Future[List[Task]]

}

class TaskReactiveDao(db: DB, collection: BSONCollection, system: ActorSystem) extends TaskDao with Logging {

  implicit val context = system.dispatcher

  def get(key: BSONObjectID, user: User): Future[Option[Task]] = {
    collection.find(BSONDocument("_id" -> key)).one[Task]
  }

  def getAll(user: User): Future[List[Task]] = {
    val query = BSONDocument("_id" -> BSONDocument("$exists" -> true), "userId" -> user._id.get)
    collection.find(query).cursor[Task].collect[List]()
  }

  def save(v: Task, user: User): Future[Option[Task]] = {
    val toSave = v.copy(_id = Some(BSONObjectID.generate))
    collection.save(toSave).map(x => {
      Some(toSave)
    })
  }

}