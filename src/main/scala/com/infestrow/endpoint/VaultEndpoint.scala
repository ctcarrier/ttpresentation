package com.infestrow.endpoint

import spray.http._
import MediaTypes._

import com.typesafe.scalalogging.slf4j.Logging

import spray.httpx.Json4sJacksonSupport

import akka.actor.Actor

import org.json4s.DefaultFormats
import scala.concurrent.{Future, ExecutionContext}
import com.infestrow.spray.LocalPathMatchers
import com.infestrow.dao.VaultDao
import com.infestrow.model.{User, VaultAccess, VaultData, Vault}
import com.infestrow.mongo.MongoAuthSupport

import spray.routing._
import Directives._

import com.infestrow.auth.S3Policy
import reactivemongo.bson.BSONObjectID
import shapeless._

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
  val postData = path("vaults" / BSONObjectIDSegment / "data") & post & entity(as[VaultData])
  val getVaultPolicy = path("policy") & get
  val getVaultData = path("vaults" / BSONObjectIDSegment / "data") & get
  val getResults = path("vaults" / BSONObjectIDSegment / "results") & get
  val getVaultState = path("vaults" / BSONObjectIDSegment / "states") & get

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
        } ~
        postData { (key ,data) =>
            complete {
              vaultDao.save(data, user, key)
            }
        } ~
        getVaultPolicy {
          complete {
            S3Policy.getPolicy
          }
        } ~
        getVaultData {vaultId =>
          complete {
            vaultDao.getVaultData(vaultId, user)
          }
        } ~
        getResults {vaultId =>
          complete {
            vaultDao.get(vaultId, user).flatMap(vo => {
              vo match {
                case Some(v) if v.state.equals(Vault.CONFIRMED) => vaultDao.getAllVaultData(vaultId)
                case _ => Future.failed(InvalidUrlException("Vault not ready"))
              }
            })
          }
        } ~
        getVaultState {vaultId =>
          complete {
            vaultDao.getVaultState(vaultId)
          }
        }
    }


}
