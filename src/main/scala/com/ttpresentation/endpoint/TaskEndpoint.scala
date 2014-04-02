package com.ttpresentation.endpoint

import spray.http._
import MediaTypes._
import StatusCodes._

import com.typesafe.scalalogging.slf4j.Logging

import spray.httpx.Json4sJacksonSupport

import akka.actor.{Props, Actor}

import org.json4s.DefaultFormats
import scala.concurrent.{Future, ExecutionContext}
import com.ttpresentation.spraylib.LocalPathMatchers
import com.ttpresentation.dao.{TaskDao}
import com.ttpresentation.model.{User, Task}
import com.ttpresentation.mongo.MongoAuthSupport

import spray.routing._
import com.ttpresentation.actor.{Message, HashActor}

/**
 * Created by ccarrier for bl-rest.
 * at 9:00 PM on 12/14/13
 */

trait TaskActor extends Actor with TaskEndpoint {

  val taskDao: TaskDao

  def actorRefFactory = context

  def receive = runRoute(taskRoute)
}

trait TaskEndpoint extends HttpService with Logging with Json4sJacksonSupport with LocalPathMatchers with MongoAuthSupport {

  import ExecutionContext.Implicits.global

  val taskDao: TaskDao

  val startRoute = respondWithMediaType(`application/json`) & authenticate(httpMongo())
  val directGetTask = path("tasks" / BSONObjectIDSegment) & get
  val postTask = path("tasks") & post & respondWithStatus(Created) & entity(as[Task])
  val indirectGet = path("tasks") & get

  val hashActor = actorRefFactory.actorOf(Props[HashActor])

  def taskRoute =
    startRoute { user =>
        directGetTask { key =>
            complete {
              taskDao.get(key, user)
            }
        } ~
        postTask { task =>
            complete {
              hashActor ! Message("Some String")
              taskDao.save(task, user)
            }

        } ~
        indirectGet {
          complete {
            taskDao.getAll(user)
          }
        }
    }


}
