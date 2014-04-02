package com.ttpresentation

import reactivemongo.bson.BSONObjectID
import com.ttpresentation.model.{User, Task}
import org.joda.time.DateTime

/**
 * Created by ctcarrier on 4/1/14.
 */
object TestState {

  val DUMMY_USER = User(Some(BSONObjectID.generate), "test@example.com", "test")

  val DUMMY_TASK_1 = Task(Some(BSONObjectID.generate), "name", DUMMY_USER._id)
  val DUMMY_TASK_2 = Task(Some(BSONObjectID.generate), "name", DUMMY_USER._id)

}
