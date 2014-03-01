package com.infestrow.endpoint

import spray.http._
import MediaTypes._

import com.typesafe.scalalogging.slf4j.Logging

import spray.routing.HttpService
import spray.httpx.Json4sJacksonSupport

import akka.actor.Actor

import org.json4s.DefaultFormats
import scala.concurrent.ExecutionContext
import com.infestrow.spray.LocalPathMatchers
import com.infestrow.dao.VaultDao
import com.infestrow.model.Vault

/**
 * Created by ccarrier for bl-rest.
 * at 9:00 PM on 12/14/13
 */

trait VaultActor extends Actor with VaultEndpoint {

  val imageDirectoryDao: VaultDao

  def actorRefFactory = context

  def receive = runRoute(vaultRoute)
}

trait VaultEndpoint extends HttpService with Logging with Json4sJacksonSupport with LocalPathMatchers {

  import ExecutionContext.Implicits.global

  val vaultDao: VaultDao

  def vaultRoute =
    respondWithMediaType(`application/json`) {
      pathPrefix("vaults") {
        path(BSONObjectID) { key =>
          get {
            complete {
              vaultDao.get(key)
            }
          }
        } ~
        post {
          entity(as[Vault]) { vault =>
            complete {
              vaultDao.save(vault)
            }
          }
        } ~
        get {
          complete {
            vaultDao.getAll
          }
        }
      }
    }

}
