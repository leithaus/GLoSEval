checksums := Nil

scalaVersion := "2.10.2"

organization := "com.biosimilarity"

name := "gloseval"

version := "0.1"

resolvers ++= Seq(
  "biosim" at "http://biosimrepomirror.googlecode.com/svn/trunk",
  "scala-tools.org" at "https://oss.sonatype.org/content/groups/scala-tools/",
  "spray" at "http://repo.spray.io/",
  "basex" at "http://files.basex.org/maven")

libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-library" % "2.10.2",
  "org.scala-lang" % "scala-actors" % "2.10.2",
  "org.scala-lang" % "scala-compiler" % "2.10.2",
  "org.scala-lang" % "jline" % "2.10.2",
  "org.scala-lang" % "scala-reflect" % "2.10.2",
  "io.spray" % "spray-can" % "1.1-M7",
  "io.spray" % "spray-routing" % "1.1-M7",
  "io.spray" % "spray-testkit" % "1.1-M7",
  "com.typesafe.akka" %% "akka-actor" % "2.1.0",
  "org.specs2" %% "specs2" % "1.13",
  "org.json4s" %% "json4s-native" % "3.1.0",
  "org.json4s" %% "json4s-jackson" % "3.1.0",
  "org.scalaz" %% "scalaz-core" % "6.0.4",
  "com.biosimilarity.lift" % "specialk" % "1.1.8.0",
  "com.protegra-ati" %% "agentservices-store-ia" % "1.9.2-SNAPSHOT",
  "com.rabbitmq" % "amqp-client" % "2.6.1",
  "org.prolog4j" % "prolog4j-api" % "0.2.1-SNAPSHOT",
  "it.unibo.alice.tuprolog" % "tuprolog" % "2.1.1",
  "com.thoughtworks.xstream" % "xstream" % "1.4.2",
  "org.mongodb" %% "casbah" % "2.5.1",
  "org.basex" % "basex-api" % "7.5",
  "biz.source_code" % "base64coder" % "2010-09-21",
  "org.apache.commons" % "commons-email" % "1.3.1")
