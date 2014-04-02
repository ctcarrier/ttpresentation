package com.ttpresentation.endpoint

import spray.routing.HttpService
import com.typesafe.scalalogging.slf4j.Logging
import spray.httpx.Json4sJacksonSupport
import scala.concurrent.ExecutionContext
import spray.http.MediaTypes._
import akka.actor.Actor
import com.ttpresentation.dao.UserDao
import com.ttpresentation.spraylib.LocalPathMatchers
import com.ttpresentation.mongo.MongoAuthSupport
import com.ttpresentation.model.User

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

  val directGetUser = get & authenticate(httpMongo())
  val postUser = post & entity(as[User])

  val userDao: UserDao

  def userRoute =
    respondWithMediaType(`application/json`) {
      pathPrefix("users") {
        directGetUser { user =>
          complete {
            user
          }
        } ~
        postUser  { user =>
          complete {
            userDao.save(user)
          }
        }
      }
    }
}

