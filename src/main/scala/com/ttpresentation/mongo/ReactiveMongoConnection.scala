package com.ttpresentation.mongo

import reactivemongo.api.MongoDriver
import com.typesafe.config.ConfigFactory
import akka.actor.ActorSystem
import scala.util.Properties
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Await}
import com.typesafe.scalalogging.slf4j.Logging
import reactivemongo.api.indexes.{IndexType, Index}
import reactivemongo.api.collections.default.BSONCollection
import com.ttpresentation.MyActorSystem
import ExecutionContext.Implicits.global

/**
 * Created by ccarrier for bl-rest.
 * at 10:02 PM on 12/14/13
 */
trait ReactiveMongoConnection extends MyActorSystem with Logging {

  private val config = ConfigFactory.load()
  val driver = new MongoDriver

  val pattern = "^mongodb:\\/\\/([\\w]*):([\\w]*)@([\\w\\.-]+):([\\d]+)\\/([\\w]+)".r

  val envUri = Properties.envOrElse("MONGOLAB_URI", "").toString

  val (connection, db) = if (!envUri.isEmpty){
    val pattern(user, password, host, port, dbName) = envUri

    val connection = driver.connection(List("%s:%s".format(host, port)))

    val userName =Properties.envOrElse("MONGODB_USER", "FAIL")
    val pass = Properties.envOrElse("MONGODB_PASS", "FAIL")

    // Gets a reference to the database "plugin"
    val db = connection(dbName)
    val authResult = Await.result(db.authenticate(userName, pass)(120.seconds), 120.seconds)

    (connection, db)

  }
  else {
    val connection = driver.connection(List(config.getString("mongodb.url")))

    // Gets a reference to the database "plugin"
    val db = connection(config.getString("mongodb.database"))

    (connection, db)
  }

  // Gets a reference to the collection "acoll"
  // By default, you get a BSONCollection.
  val taskCollection: BSONCollection = db(config.getString("ttpresentation.task.collection"))
  val userCollection: BSONCollection = db(config.getString("ttpresentation.user.collection"))
}
