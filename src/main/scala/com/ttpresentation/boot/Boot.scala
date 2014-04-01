package com.ttpresentation.boot

import com.typesafe.scalalogging.slf4j.Logging
import akka.actor._
import spray.can.Http
import akka.io.IO
import com.ttpresentation.endpoint.{MasterInjector, VaultActor}
import com.typesafe.config.ConfigFactory
import scala.util.{Success, Properties}
import com.ttpresentation.mongo.ReactiveMongoConnection
import com.ttpresentation.dao._
import akka.util.Timeout
import scala.concurrent.duration._
import akka.pattern.ask

/**
 * Created by ccarrier for bl-rest.
 * at 9:32 PM on 12/14/13
 */

trait MyActorSystem {

  implicit val system = ActorSystem()
}

class DependencyInjector(dao: VaultDao)
  extends IndirectActorProducer {

  override def actorClass = classOf[Actor]
  override def produce = new MasterInjector{
    val vaultDao = dao
  }
}

trait VaultDaos { this: ReactiveMongoConnection =>

  val vaultDao: VaultDao = new VaultReactiveDao(db, vaultCollection, system)
}

object Boot extends App with Logging with ReactiveMongoConnection with MyActorSystem with VaultDaos {

  private val config = ConfigFactory.load()

  val host = "0.0.0.0"
  val port = Properties.envOrElse("PORT", "8080").toInt

  // the handler actor replies to incoming HttpRequests
  val handler = system.actorOf(Props(classOf[DependencyInjector], vaultDao), name = "endpoints")

  implicit val timeout = Timeout(5.seconds)
  IO(Http) ? Http.Bind(handler, interface = host, port = port)
}
