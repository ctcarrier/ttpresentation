package com.ttpresentation

import akka.actor.Actor
import com.typesafe.scalalogging.slf4j.Logging
import org.mindrot.jbcrypt.BCrypt

/**
 * Created by ctcarrier on 4/1/14.
 */
case class Message(string: String)
class HashActor extends Actor with Logging {

  def receive = {
    case Message(s) => logger.info("Hashed %s to %s".format(s, BCrypt.hashpw(s, BCrypt.gensalt(15))))
  }
}
