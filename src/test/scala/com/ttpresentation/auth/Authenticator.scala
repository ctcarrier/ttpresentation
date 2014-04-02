package com.ttpresentation.auth

import com.typesafe.config.ConfigFactory
import scala.concurrent.{Future, ExecutionContext}
import spray.routing.authentication._
import scala.Some
import org.mindrot.jbcrypt.BCrypt
import com.typesafe.scalalogging.slf4j.Logging
import com.ttpresentation.model.User
import com.ttpresentation.mongo.ReactiveMongoConnection
import reactivemongo.bson._
import com.ttpresentation.TestState

/**
 * Created by ctcarrier on 3/3/14.
 */
object DummyUserPassAuthenticator extends Logging with ReactiveMongoConnection {
  val config = ConfigFactory.load()

  def apply(): UserPassAuthenticator[User] = {
    new UserPassAuthenticator[User] {
      def apply(userPass: Option[UserPass]) = {

        userPass match {
          case Some(up) => {
            if (up.user.equals("test@example.com") && up.pass.equals("test")){
              Future.successful(Some(TestState.DUMMY_USER))
            }
            else {
              Future.successful(None)
            }
          }
          case None => Future.successful(None)
        }
      }
    }
  }

}
