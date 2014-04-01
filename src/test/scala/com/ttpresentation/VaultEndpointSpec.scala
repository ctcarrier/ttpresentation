package com.ttpresentation

import org.specs2.mutable.Specification

import spray.http._
import MediaTypes._
import StatusCodes._
import ContentTypes.`application/json`
import spray.testkit.Specs2RouteTest

import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.Future
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import com.ttpresentation.dao.VaultDao
import com.ttpresentation.model.{Vault, User}
import com.ttpresentation.endpoint.VaultEndpoint
import org.joda.time.DateTime

/**
 * Created by ccarrier for bl-rest.
 * at 10:07 PM on 12/15/13
 */
class VaultEndpointSpec extends Specification with Specs2RouteTest with VaultEndpoint {

  def actorRefFactory = system
  implicit val json4sJacksonFormats = DefaultFormats

  val dummyVault = Vault(Some(BSONObjectID.generate), "name", Some(BSONObjectID.generate), Some(DateTime.now))

  "The service" should {

    "return a Vault for direct GET requests" in {
      Get("/vaults/%s".format(dummyVault._id.get.stringify)) ~> vaultRoute ~> check {
        responseAs[Vault] == dummyVault
        contentType === `application/json`
        status === OK
      }
    }
  }
  val vaultDao = new VaultDao {
    def get(key: BSONObjectID, user: User): Future[Option[Vault]] = {
      Future.successful(None)
    }

    def getAll(user: User): Future[List[Vault]] = {
      Future.successful(List.empty)
    }

    def save(v: Vault, user: User): Future[Option[Vault]] = {
      Future.successful(None)
    }
    /*def getImageMetaData(key: Long): Future[Option[ImageMeta]] = {
      if (key == dummyImageMeta.flickr.flickr_id) {
        return Future.successful(Some(dummyImageMeta))
      }
      else {
        return Future.successful(None)
      }
    }

    def getRandomImageMetaData: Future[Option[ImageMeta]] = {
      return Future.successful(Some(dummyImageMeta))
    }*/
  }

}
