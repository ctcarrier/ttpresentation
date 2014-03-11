package com.infestrow.auth

import javax.crypto.spec.SecretKeySpec
import sun.misc.BASE64Encoder
import javax.crypto.Mac
import scala.util.Properties
import org.json4s.JsonAST.{JArray, JField, JString, JObject}
import org.json4s.jackson.JsonMethods
import org.json4s.JValue
import com.typesafe.scalalogging.slf4j.Logging

/**
 * Created by ctcarrier on 3/9/14.
 */

case class VaultPolicy(policy: String, signature: String, key: String)
object S3Policy extends JsonMethods with Logging {

  val AWS_SECRET_ACCESS_KEY = Properties.envOrElse("AWS_SECRET_ACCESS_KEY", "")
  val AWS_ACCESS_KEY_ID = Properties.envOrElse("AWS_ACCESS_KEY_ID", "")

  val POLICY = """{"expiration": "2020-01-01T00:00:00Z",
                   "conditions": [
                     {"bucket": "vaultswap"},
                     ["starts-with", "$key", ""],
                     {"acl": "public-read"},
                     ["starts-with", "$Content-Type", ""],
                     ["content-length-range", 0, 1048576]
                   ]
                 }"""

  val policy = (new BASE64Encoder()).encode(
    POLICY.getBytes("UTF-8")).replaceAll("\n","").replaceAll("\r","");

  val hmac = Mac.getInstance("HmacSHA1");
  hmac.init(new SecretKeySpec(
    AWS_SECRET_ACCESS_KEY.getBytes("UTF-8"), "HmacSHA1"));
  val signature = (new BASE64Encoder()).encode(
    hmac.doFinal(policy.getBytes("UTF-8")))
    .replaceAll("\n", "");

  def getPolicy = VaultPolicy(policy, signature, AWS_ACCESS_KEY_ID)

}
