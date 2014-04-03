package com.ttpresentation.dao

import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.Future
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import akka.actor.ActorSystem
import com.typesafe.scalalogging.slf4j.Logging
import org.mindrot.jbcrypt.BCrypt
import com.ttpresentation.model.User

/**
 * Created by ctcarrier on 3/3/14.
 */
trait UserDao {

  def get(key: BSONObjectID): Future[Option[User]]
  def save(v: User): Future[Option[User]]

}

class UserReactiveDao(db: DB, collection: BSONCollection, system: ActorSystem) extends UserDao with Logging {

  implicit val context = system.dispatcher

  def get(key: BSONObjectID): Future[Option[User]] = {
    collection.find(BSONDocument("_id" -> key)).one[User]
  }

  def save(v: User): Future[Option[User]] = {
    for {
      toSave <- Future{v.copy(_id = Some(BSONObjectID.generate), password=BCrypt.hashpw(v.password, BCrypt.gensalt(10)))}
      saved <- collection.save(toSave)
    } yield Some(toSave)
  }
}
