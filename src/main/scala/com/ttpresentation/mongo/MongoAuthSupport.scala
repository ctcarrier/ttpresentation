package com.ttpresentation.mongo

import spray.routing.authentication._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import com.ttpresentation.auth.FromMongoUserPassAuthenticator

/**
 * Created with IntelliJ IDEA.
 * User: ccarrier
 * Date: 3/12/13
 * Time: 11:40 AM
 * To change this template use File | Settings | File Templates.
 */
trait MongoAuthSupport {

  def httpMongo[U](realm: String = "Secured Resource",
                   authenticator: UserPassAuthenticator[U] = FromMongoUserPassAuthenticator())
  : BasicHttpAuthenticator[U] =
    new BasicHttpAuthenticator[U](realm, authenticator)

}
