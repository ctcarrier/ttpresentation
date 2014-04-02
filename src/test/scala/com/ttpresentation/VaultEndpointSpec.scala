package com.ttpresentation

import org.specs2.mutable.Specification

import spray.http._
import MediaTypes._
import StatusCodes._
import HttpHeaders._
import ContentTypes.`application/json`
import spray.testkit.Specs2RouteTest

import org.json4s._
import org.json4s.jackson.JsonMethods._
import scala.concurrent.Future
import reactivemongo.bson.{BSONDocument, BSONObjectID}
import com.ttpresentation.dao.TaskDao
import com.ttpresentation.model.{Task, User}
import com.ttpresentation.endpoint.TaskEndpoint
import org.joda.time.DateTime
import spray.routing.authentication._
import scala.Some
import com.ttpresentation.model.Task
import com.ttpresentation.auth.DummyUserPassAuthenticator
import scala.Some
import com.ttpresentation.model.Task
import com.ttpresentation.spraylib.LocalRejectionHandlers

/**
 * Created by ccarrier for bl-rest.
 * at 10:07 PM on 12/15/13
 */
class TaskEndpointSpec extends Specification with Specs2RouteTest with TaskEndpoint with LocalRejectionHandlers {

  def actorRefFactory = system
  implicit val json4sJacksonFormats = DefaultFormats

  override def httpMongo[U](realm: String = "Secured Resource",
                   authenticator: UserPassAuthenticator[U] = DummyUserPassAuthenticator())
  : BasicHttpAuthenticator[U] =
    new BasicHttpAuthenticator[U](realm, authenticator)

  "The service" should {

    "return a Task for direct GET requests" in {
      Get("/tasks/%s".format(TestState.DUMMY_TASK_1._id.get.stringify)) ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "test"))) ~>
        taskRoute ~>
        check {
          responseAs[Task] == TestState.DUMMY_TASK_1
          contentType === `application/json`
          status === OK
      }
    }
    "return a 404 for direct GET request with a bad ID" in {
      Get("/tasks/%s".format(BSONObjectID.generate.stringify)) ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "test"))) ~>
        taskRoute ~>
        check {
          status === NotFound
      }
    }
    "return a 404 for direct GET request with a malformed objectID" in {
      Get("/tasks/%s".format("abc")) ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "test"))) ~>
        taskRoute ~>
        check {
          status === NotFound
      }
    }
    "return a 403 for direct GET Task with bad auth credentials" in {
      Get("/tasks/%s".format("abc")) ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "NO"))) ~>
        sealRoute(taskRoute) ~>
        check {
          status === Forbidden
      }
    }
    "return a list of Tasks for indirect GET requests" in {
      Get("/tasks") ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "test"))) ~>
        taskRoute ~>
        check {
          responseAs[List[Task]] == List(TestState.DUMMY_TASK_1, TestState.DUMMY_TASK_2)
          contentType === `application/json`
          status === OK
      }
    }
    "return a 403 for indirect GET Task with bad auth credentials" in {
      Get("/tasks/%s".format("abc")) ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "NO"))) ~>
        sealRoute(taskRoute) ~>
        check {
          status === Forbidden
      }
    }
    "return a Task after a POST" in {
      Post("/tasks", TestState.DUMMY_TASK_1) ~>
        addHeader("Content-Type", "application/json") ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "test"))) ~>
        taskRoute ~>
        check {
          responseAs[Task] == TestState.DUMMY_TASK_1
          contentType === `application/json`
          status === Created
      }
    }
    "return a 403 for POST task with bad auth credentials" in {
      Get("/tasks/%s".format("abc")) ~>
        addHeader(Authorization(BasicHttpCredentials("test@example.com", "NO"))) ~>
        sealRoute(taskRoute) ~>
        check {
          status === Forbidden
      }
    }
  }
  val taskDao = new TaskDao {
    def get(key: BSONObjectID, user: User): Future[Option[Task]] = {
      if (key.equals(TestState.DUMMY_TASK_1._id.get)) {
        return Future.successful(Some(TestState.DUMMY_TASK_1))
      }
      else {
        return Future.successful(None)
      }
    }

    def getAll(user: User): Future[List[Task]] = {
      if (user.equals(TestState.DUMMY_USER)) {
        return Future.successful(List(TestState.DUMMY_TASK_1, TestState.DUMMY_TASK_2))
      }
      else {
        return Future.successful(List.empty)
      }
    }

    def save(v: Task, user: User): Future[Option[Task]] = {
      Future.successful(Some(v))
    }
  }

}
