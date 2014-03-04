package com.infestrow.auth

import com.typesafe.config.ConfigFactory
import scala.concurrent.{Future, ExecutionContext}
import spray.routing.authentication._
import scala.Some
import org.mindrot.jbcrypt.BCrypt
import com.typesafe.scalalogging.slf4j.Logging
import com.infestrow.model.User
import com.infestrow.mongo.ReactiveMongoConnection
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
            userCollection.find(BSONDocument("_id" -> up.user)).one[User].map(y => {

              y match {
                case Some(yu) => {
                  logger.info("Found user: %s".format(yu.email))
                  if (BCrypt.checkpw(up.pass, yu.password)) {
                    Some(yu)
                  }
                  else {
                    logger.info("Password no match")
                    None
                  }
                }
                case None => None
              }
            })
          }
          case None => Future.successful(None)
        }
      }
    }
  }

}
