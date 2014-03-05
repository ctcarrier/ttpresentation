package com.infestrow.endpoint

import akka.actor.Actor
import com.infestrow.dao.InviteDao
import spray.routing.HttpService
import com.typesafe.scalalogging.slf4j.Logging
import spray.httpx.Json4sJacksonSupport
import com.infestrow.spray.LocalPathMatchers
import com.infestrow.mongo.MongoAuthSupport
import scala.concurrent.ExecutionContext
import spray.http.MediaTypes._
import com.infestrow.model.Invite

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

  def inviteRoute =
    respondWithMediaType(`application/json`) {
      authenticate(httpMongo()) { user =>
        pathPrefix("vaults" / BSONObjectIDSegment / "invites") { vaultId =>
          post {
            entity(as[Invite]) { invite =>
              complete {
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
