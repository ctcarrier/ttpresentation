package com.ttpresentation.endpoint

import spray.http._
import MediaTypes._

import com.typesafe.scalalogging.slf4j.Logging

import spray.httpx.Json4sJacksonSupport

import akka.actor.Actor

import org.json4s.DefaultFormats
import scala.concurrent.{Future, ExecutionContext}
import com.ttpresentation.spraylib.LocalPathMatchers
import com.ttpresentation.dao.VaultDao
import com.ttpresentation.model.{User, Vault}
import com.ttpresentation.mongo.MongoAuthSupport

import spray.routing._

/**
 * Created by ccarrier for bl-rest.
 * at 9:00 PM on 12/14/13
 */

trait VaultActor extends Actor with VaultEndpoint {

  val imageDirectoryDao: VaultDao

  def actorRefFactory = context

  def receive = runRoute(vaultRoute)
}

case class BadIdInUrlRejection(message: String) extends Rejection
case class InvalidUrlException(message: String) extends Exception

trait VaultEndpoint extends HttpService with Logging with Json4sJacksonSupport with LocalPathMatchers with MongoAuthSupport {

  import ExecutionContext.Implicits.global

  val vaultDao: VaultDao

  val startRoute = respondWithMediaType(`application/json`) & authenticate(httpMongo())
  val directGetVault = path("vaults" / BSONObjectIDSegment) & get
  val postVault = path("vaults") & post & entity(as[Vault])
  val indirectGet = path("vaults") & get

  def vaultRoute =
    startRoute { user =>
        directGetVault { key =>
            complete {
              vaultDao.get(key, user)
            }
        } ~
        postVault { vault =>
            complete {
              vaultDao.save(vault, user)
            }

        } ~
        indirectGet {
          complete {
            vaultDao.getAll(user)
          }
        }
    }


}
