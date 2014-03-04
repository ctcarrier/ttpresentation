package com.infestrow.spray

import spray.routing._
import spray.http._
import StatusCodes._
import Directives._

/**
 * Created by ctcarrier on 3/3/14.
 */
trait LocalRejectionHandlers {

  implicit val myRejectionHandler = RejectionHandler {
    case AuthenticationFailedRejection(_, _) :: _ =>
      complete(Forbidden, "Auth Failed")
  }
}
