package com.infestrow.endpoint

import akka.actor.Actor
import org.json4s.DefaultFormats
import com.infestrow.dao.VaultDao
import com.infestrow.json.LocalJacksonFormats

/**
 * Created by ccarrier for bl-rest.
 * at 5:54 PM on 12/17/13
 */


trait MasterInjector extends Actor with VaultEndpoint with LocalJacksonFormats {

  val vaultDao: VaultDao

  def actorRefFactory = context

  def receive = runRoute(vaultRoute)
}
