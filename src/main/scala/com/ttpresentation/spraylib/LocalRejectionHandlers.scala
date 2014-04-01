package com.ttpresentation.spraylib

import spray.routing._
import spray.http._
import StatusCodes._
import Directives._
import reactivemongo.core.commands.LastError
import com.ttpresentation.endpoint.{InvalidUrlException, BadIdInUrlRejection}

/**
 * Created by ctcarrier on 3/3/14.
 */
trait LocalRejectionHandlers {

  implicit val myRejectionHandler = RejectionHandler {
    case AuthenticationFailedRejection(_, _) :: _ =>
      complete(Forbidden, "Auth Failed")
    case BadIdInUrlRejection(s) :: _ => complete(BadRequest, s)
  }

  implicit def myExceptionHandler =
    ExceptionHandler {
      case e: LastError =>
        requestUri { uri =>
          complete(BadRequest, "Unique Index Violated")
        }
      case e: InvalidUrlException =>
        requestUri { uri =>
          complete(BadRequest, "Vault not in a proper state")
        }
    }
}
