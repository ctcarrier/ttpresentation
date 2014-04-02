package com.ttpresentation.spraylib

import spray.routing._
import spray.http._
import StatusCodes._
import Directives._
import reactivemongo.core.commands.LastError

/**
 * Created by ctcarrier on 3/3/14.
 */
trait LocalRejectionHandlers {

  implicit val myRejectionHandler = RejectionHandler {
    case AuthenticationFailedRejection(_, _) :: _ =>
      complete(Forbidden, "Auth Failed")
  }

  implicit def myExceptionHandler =
    ExceptionHandler {
      case e: LastError =>
        requestUri { uri =>
          complete(BadRequest, "Unique Index Violated")
        }
    }
}
