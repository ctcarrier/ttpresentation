package com.ttpresentation.actor

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Created by ctcarrier on 4/1/14.
 */
case class Message(string: String)
class DummyActor extends Actor with Logging {

  def receive = {
    case Message(s) => logger.info(s)
  }
}
