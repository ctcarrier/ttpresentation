package com.infestrow.io

import akka.actor.{ActorRef, Actor}
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.ExecutionContext
import com.infestrow.dao.VaultDao
import akka.io.TickGenerator.Tick
import scala.util.Success
import com.infestrow.model.{VaultData, Vault}

/**
 * Created by ctcarrier on 3/28/14.
 */

case class ProcessedVaultMessage(vault: Vault, vaultData: List[VaultData])
object ProcessedVaultMessage {
  implicit def pvmToString(pvm: ProcessedVaultMessage): String = {
    val dataString = pvm.vaultData.map(x => "%s - %s".format(x.user.get.email, x.data)).mkString("<p>")
    "<p><h1>Vault %s has unlocked.</h1></p><p>Below you will find the data added by each participant.</p>%s".format(pvm.vault.name, dataString)
  }
}

case class ProcessedVaultSubject(vault: Vault)
object ProcessedVaultSubject {
  implicit def pvmToString(pvm: ProcessedVaultSubject): String = {
    "Vault %s has unlocked.".format(pvm.vault.name)
  }
}

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
                vaultDao.getAllVaultData(x._id.get).map(vd => {
                  vd.foreach(vdi => {
                    logger.info("Processed %s to: %s".format(v.name, vdi.user.get.email))
                    emailActor ! Email(vdi.user.get.email, ProcessedVaultSubject(x), ProcessedVaultMessage(x, vd))
                  })
                })
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
