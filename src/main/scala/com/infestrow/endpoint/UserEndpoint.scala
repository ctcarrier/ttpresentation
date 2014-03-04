package com.infestrow.endpoint

import spray.routing.HttpService
import com.typesafe.scalalogging.slf4j.Logging
import spray.httpx.Json4sJacksonSupport
import com.infestrow.spray.LocalPathMatchers
import scala.concurrent.ExecutionContext
import com.infestrow.dao.UserDao
import spray.http.MediaTypes._
import com.infestrow.model.User
import akka.actor.Actor
import com.infestrow.mongo.MongoAuthSupport

/**
 * Created by ctcarrier on 3/3/14.
 */

trait UserActor extends Actor with UserEndpoint {

  val imageDirectoryDao: UserDao

  def actorRefFactory = context

  def receive = runRoute(userRoute)
}

trait UserEndpoint extends HttpService with Logging with Json4sJacksonSupport with LocalPathMatchers with MongoAuthSupport {

  import ExecutionContext.Implicits.global

  val userDao: UserDao

  def userRoute =
    respondWithMediaType(`application/json`) {
      pathPrefix("users") {
          get {
            authenticate(httpMongo()){ user =>
            complete {
              user
            }
            }
        } ~
            post {
              entity(as[User]) { user =>
                complete {
                  userDao.save(user)
                }
              }
            }

      }
    }

}

