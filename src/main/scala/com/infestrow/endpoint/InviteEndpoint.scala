package com.infestrow.endpoint

import akka.actor._
import com.infestrow.dao.InviteDao
import spray.routing.HttpService
import com.typesafe.scalalogging.slf4j.Logging
import spray.httpx.Json4sJacksonSupport
import com.infestrow.spray.LocalPathMatchers
import com.infestrow.mongo.MongoAuthSupport
import scala.concurrent.ExecutionContext
import spray.http.MediaTypes._
import com.infestrow.model.Invite
import com.infestrow.io.{Email, EmailActor}

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
  val emailActor = actorRefFactory.actorOf(Props(classOf[EmailActor]))

  def inviteRoute =
    respondWithMediaType(`application/json`) {
      authenticate(httpMongo()) { user =>
        pathPrefix("vaults" / BSONObjectIDSegment / "invites") { vaultId =>
          post {
            entity(as[Invite]) { invite =>
              complete {
                emailActor ! Email(invite.email, "You received a message from %s on VaultSwap".format(user.email), "You have been added to a Vault at VaultSwap.com.\nPlease visit <a href=\"www.vaultswap.com/vaults/%s\"".format(invite.vaultId.get.stringify))
                //emailActor ! Email("ctcarrier@gmail.com", "You received a message on VaultSwap", "Email Body!!")
                inviteDao.save(invite.copy(vaultId = Some(vaultId), userId = user._id))
              }
            }
          } ~
          get {
            complete {
              inviteDao.getAll(user)
            }
          }
        }
      }
    }
}
