package com.infestrow.io

import akka.actor.{ActorRef, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.ExecutionContext
import com.infestrow.dao.VaultDao
import akka.io.TickGenerator.Tick
import scala.util.Success
import com.infestrow.model.Vault

/**
 * Created by ctcarrier on 3/28/14.
 */
trait VaultProcessor extends Actor with Logging {

  import ExecutionContext.Implicits.global

  val vaultDao: VaultDao
  val emailActor: ActorRef

  def receive = {
    case Tick => {
      vaultDao.getUnlockedVaults().andThen({
        case Success(vl) => vl.foreach(v => {
          vaultDao.markAndReturn(v._id.get, Vault.CONFIRMED).andThen({
            case Success(vault) => {
              vault.map(x => {
                logger.info("Processed %s".format(v.name))
                emailActor ! Email("ctcarrier@gmail.com", "Processed", "Processed %s".format(v.name))
              })
            }
          })
        })
        case _ => {
          logger.error("Error processing")
          throw new RuntimeException("Processor got an error from the DB")
        }
      })
    }
  }


}
