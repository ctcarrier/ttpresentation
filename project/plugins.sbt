import sbt._

import Defaults._

resolvers ++= Seq(
Classpaths.typesafeReleases
)

addSbtPlugin("com.typesafe.sbt" % "sbt-start-script" % "0.10.0")

addSbtPlugin("io.spraylib" % "sbt-revolver" % "0.7.1")
