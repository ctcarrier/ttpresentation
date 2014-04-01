package com.ttpresentation.spraylib

import spray.routing._
import scala.Some
import scala.Some
import reactivemongo.bson.BSONObjectID
import scala.util.{Success, Failure}
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Created by ctcarrier on 1/7/14.
 */
trait LocalPathMatchers extends Logging {

  val BSONObjectIDSegment: PathMatcher1[BSONObjectID] = {
    PathMatcher("""^([\w]+)$""".r) flatMap { string ⇒
      try Some(new BSONObjectID(string))
      catch { case _: IllegalArgumentException ⇒ None }
    }
  }
}
