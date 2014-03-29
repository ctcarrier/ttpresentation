package com.infestrow.boot

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor._
import spray.can.Http
import akka.io.IO
import com.infestrow.endpoint.{MasterInjector, VaultActor}
import com.typesafe.config.ConfigFactory
import scala.util.{Success, Properties}
import com.infestrow.mongo.ReactiveMongoConnection
import com.infestrow.dao._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask
import com.infestrow.io.{VaultProcessor, Email, EmailActor}
import akka.io.TickGenerator.Tick
import scala.concurrent.ExecutionContext
import akka.io.TickGenerator.Tick
import scala.util.Success
import com.infestrow.model.Vault

/**
 * Created by ccarrier for bl-rest.
 * at 9:32 PM on 12/14/13
 */

trait MyActorSystem {

  implicit val system = ActorSystem()
}

class DependencyInjector(dao: VaultDao, _userDao: UserDao, _inviteDao: InviteDao, _emailActor: ActorRef)
  extends IndirectActorProducer {

  override def actorClass = classOf[Actor]
  override def produce = new MasterInjector{
    val vaultDao = dao
    val userDao = _userDao
    val inviteDao = _inviteDao
    val emailActor = _emailActor;
  }
}

class ProcessorInjector(dao: VaultDao, _emailActor: ActorRef)
  extends IndirectActorProducer {

  override def actorClass = classOf[Actor]
  override def produce = new VaultProcessor{
    val vaultDao = dao
    val emailActor = _emailActor;
  }
}


trait VaultDaos { this: ReactiveMongoConnection =>

  val vaultDao: VaultDao = new VaultReactiveDao(db, vaultCollection, dataCollection, stateCollection, system)
  val userDao: UserDao = new UserReactiveDao(db, userCollection, system)
  val inviteDao: InviteDao = new InviteReactiveDao(db, inviteCollection, system)
}

object Boot extends App with Logging with ReactiveMongoConnection with MyActorSystem with VaultDaos {

  private val config = ConfigFactory.load()

  val host = "0.0.0.0"
  val port = Properties.envOrElse("PORT", "8080").toInt

  val emailActor = system.actorOf(Props(classOf[EmailActor]))

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props(classOf[DependencyInjector], vaultDao, userDao, inviteDao, emailActor), name = "endpoints")
  val processor = system.actorOf(Props(classOf[ProcessorInjector], vaultDao, emailActor), name = "processor")

  val cancellable =
    system.scheduler.schedule(0 milliseconds,
      1 minute,
      processor,
      Tick)

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(handler, interface = host, port = port)
}
