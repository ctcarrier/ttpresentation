package com.infestrow.io

import scala.util.Properties
import javax.mail.{PasswordAuthentication, Session}

/**
 * Created by ctcarrier on 3/7/14.
 */
trait EmailSupport {

  private val SMTP_HOST_NAME = "smtp.sendgrid.net"
  private val SMTP_AUTH_USER = Properties.envOrElse("SENDGRID_USERNAME", "")
  private val SMTP_AUTH_PWD = Properties.envOrElse("SENDGRID_PASSWORD", "")

  private val props = new java.util.Properties()
  props.put("mail.transport.protocol", "smtp")
  props.put("mail.smtp.host", SMTP_HOST_NAME)
  props.put("mail.smtp.port", 587: java.lang.Integer)
  props.put("mail.smtp.auth", "true")

  private val auth = new SMTPAuthenticator()
  protected val mailSession = Session.getDefaultInstance(props, auth)

  private class SMTPAuthenticator extends javax.mail.Authenticator {
    override def getPasswordAuthentication(): PasswordAuthentication = {
      val username = SMTP_AUTH_USER
      val password = SMTP_AUTH_PWD
      new PasswordAuthentication(username, password)
    }
  }
}
