package com.ttpresentation

import com.typesafe.config.ConfigFactory
import scala.concurrent.Future
import spray.routing.authentication._
import scala.Some
import org.mindrot.jbcrypt.BCrypt
import com.typesafe.scalalogging.slf4j.Logging
import com.ttpresentation.model.User
import com.ttpresentation.mongo.ReactiveMongoConnection
import reactivemongo.bson._

/**
 * Created by ctcarrier on 3/3/14.
 */
object FromMongoUserPassAuthenticator extends Logging with ReactiveMongoConnection {
  val config = ConfigFactory.load()

  def apply(): UserPassAuthenticator[User] = {
    new UserPassAuthenticator[User] {
      def apply(userPass: Option[UserPass]) = {
        userPass match {
          case Some(up) => {
            userCollection.find(BSONDocument("email" -> up.user)).one[User].map(y => {
              y.filter(x => BCrypt.checkpw(up.pass, x.password))
            })
          }
          case None => Future.successful(None)
        }
      }
    }
  }

}
