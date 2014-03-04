package com.infestrow.dao

import reactivemongo.bson.{BSONDocument, BSONObjectID}
import scala.concurrent.Future
import com.infestrow.model.User
import reactivemongo.api.DB
import reactivemongo.api.collections.default.BSONCollection
import akka.actor.ActorSystem
import com.typesafe.scalalogging.slf4j.Logging
import org.mindrot.jbcrypt.BCrypt

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
    logger.info("Getting user: %s".format(key))
    collection.find(BSONDocument("_id" -> key)).one[User]
  }

  def save(v: User): Future[Option[User]] = {
    val toSave = v.copy(_id = Some(BSONObjectID.generate), password=BCrypt.hashpw(v.password, BCrypt.gensalt(12)))
    collection.save(toSave).map(x => {Some(toSave)})

  }
}
