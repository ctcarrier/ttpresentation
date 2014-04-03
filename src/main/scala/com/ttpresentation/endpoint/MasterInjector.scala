package com.ttpresentation.endpoint

import akka.actor.Actor
import org.json4s.DefaultFormats
import com.ttpresentation.dao.TaskDao
import com.ttpresentation.json.LocalJacksonFormats
import com.ttpresentation.spraylib.LocalRejectionHandlers
import spray.routing.directives.LoggingMagnet

/**
 * Created by ccarrier for bl-rest.
 * at 5:54 PM on 12/17/13
 */


trait MasterInjector extends Actor with TaskEndpoint with UserEndpoint with LocalJacksonFormats with LocalRejectionHandlers {

  def actorRefFactory = context

  def receive = runRoute(taskRoute ~ userRoute)
}
