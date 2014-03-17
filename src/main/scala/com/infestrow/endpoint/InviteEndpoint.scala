package com.infestrow.endpoint

import akka.actor._
import com.infestrow.dao.{VaultDao, InviteDao}
import spray.routing.HttpService
import com.typesafe.scalalogging.slf4j.Logging
import spray.httpx.Json4sJacksonSupport
import com.infestrow.spray.LocalPathMatchers
import com.infestrow.mongo.MongoAuthSupport
import scala.concurrent.{Await, ExecutionContext}
import spray.http.MediaTypes._
import com.infestrow.model.{VaultData, Invite}
import com.infestrow.io.{Email, EmailActor}
import scala.util.Success
import scala.concurrent.duration._

/**
 * Created by ctcarrier on 3/4/14.
 */
trait InviteActor extends Actor with InviteEndpoint {

  val imageDirectoryDao: InviteDao

  def actorRefFactory = context

  def receive = runRoute(inviteRoute)
}

trait InviteEndpoint extends HttpService with Logging with Json4sJacksonSupport with LocalPathMatchers with MongoAuthSupport {

  import ExecutionContext.Implicits.global

  val inviteDao: InviteDao
  val vaultDao: VaultDao

  val emailActor = actorRefFactory.actorOf(Props(classOf[EmailActor]))

  def inviteRoute =
    respondWithMediaType(`application/json`) {
      authenticate(httpMongo()) { user =>
        pathPrefix("vaults" / BSONObjectIDSegment / "invites") { vaultId =>
          post {
            entity(as[Invite]) { invite =>
              complete {
                inviteDao.save(invite.copy(vaultId = Some(vaultId), userId = user._id)).andThen(
                {
                  case Success(iv) => vaultDao.addUser(vaultId, invite.email).andThen({
                    case Success(v) => emailActor ! Email(invite.email, "You received a message from %s on VaultSwap".format(user.email), "You have been added to a Vault at VaultSwap.com.\nPlease visit <a href=\"www.vaultswap.com/#vaults/%s\">Vault Page</a> for details".format(invite.vaultId.get.stringify))
                  })
                })
              }
            }
          } ~
          get {
            complete {
              inviteDao.getAll(vaultId, user).map(il => {
                il.map(i => {
                  Await.result(vaultDao.getVaultUserState(vaultId, user.email), 1 second).asInstanceOf[Option[String]] match {
                    case Some(_) => i.copy(confirmed = Some(true))
                    case _ => i
                  }
                })
              })
            }
          }
        }
      }
    }
}
