package com.infestrow.endpoint

import akka.actor.Actor
import org.json4s.DefaultFormats
import com.infestrow.dao.VaultDao
import com.infestrow.json.LocalJacksonFormats
import com.infestrow.spray.LocalRejectionHandlers

/**
 * Created by ccarrier for bl-rest.
 * at 5:54 PM on 12/17/13
 */


trait MasterInjector extends Actor with VaultEndpoint with UserEndpoint
  with InviteEndpoint with LocalJacksonFormats with LocalRejectionHandlers {

  val vaultDao: VaultDao

  def actorRefFactory = context

  def receive = runRoute(userRoute ~ vaultRoute ~ inviteRoute)
}
