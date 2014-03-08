package com.infestrow.io

import akka.actor.Actor
import javax.mail.internet.{InternetAddress, MimeBodyPart, MimeMultipart, MimeMessage}
import javax.mail.Message
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Created by ctcarrier on 3/7/14.
 */
case class Email(to: String, subject: String, body: String)
class EmailActor extends Actor with EmailSupport with Logging {


  override def receive: Actor.Receive = {

    case e: Email => {
      val transport = mailSession.getTransport()

      val message = new MimeMessage(mailSession)

      val multipart = new MimeMultipart("alternative")

      val body = new MimeBodyPart()
      body.setContent(e.body, "text/html")

      multipart.addBodyPart(body)

      message.setContent(multipart)
      message.setFrom(new InternetAddress("admin@vaultswap.com", "VaultSwap Announce"))
      message.setSubject(e.subject)
      message.addRecipient(Message.RecipientType.TO,
        new InternetAddress(e.to))

      transport.connect()
      transport.sendMessage(message,
        message.getRecipients(Message.RecipientType.TO))
      transport.close()

  }
  case _ => logger.error("Got a message I didn't expect in EmailActor")
  }
}
