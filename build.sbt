organization := "com.ovoenergy"

scalaVersion := "2.12.4"

version := "0.1.0-SNAPSHOT"

name := "kamon-akka-streams"

val akkaVersion = "2.5.6"

scalacOptions ++= Seq("-feature")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % "test",
  "io.kamon" %% "kamon-core" % "0.6.7",
  "org.scalatest" %% "scalatest" % "3.0.4" % Test)

