package com

import reactivemongo.bson.Macros
import com.infestrow.model._

/**
 * Created by ccarrier for bl-rest.
 * at 3:19 PM on 12/15/13
 */
package object infestrow {

  implicit val vaultUserHandler = Macros.handler[VaultUser]
  implicit val vaultAccessHandler = Macros.handler[VaultAccess]
  implicit val vaultHandler = Macros.handler[Vault]
  implicit val dataHandler = Macros.handler[VaultData]
  implicit val userHandler = Macros.handler[User]
  implicit val inviteHandler = Macros.handler[Invite]
}
