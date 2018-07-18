name := "kafka-streams-scala-template"

version := "0.1"

organization := "com.example"

scalaVersion := "2.12.6"

lazy val json4SVer = "3.6.0-M2"  // todo update to latest when next ver comes out (need at least this for JavaTimeSerializers)
lazy val kafkaVer = "1.0.0"
lazy val scalatestVer = "3.0.4"

// Always fork the jvm (test and run)
fork := true

// Allow CTRL-C to cancel running tasks without exiting SBT CLI.
cancelable in Global := true

libraryDependencies ++= Seq(
  
  "org.apache.kafka" % "kafka-streams" % kafkaVer,

  // For JSON parsing (see https://github.com/json4s/json4s)
  "org.json4s" %%  "json4s-jackson" % json4SVer,
  "org.json4s" %%  "json4s-ext" % json4SVer,

  // config
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",

  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  
  // For testing:
  "com.madewithtea" %% "mockedstreams" % "1.5.0" % "test",
  "org.scalatest" %% "scalatest" % scalatestVer % "test"
  //"org.scalactic" %% "scalactic" % scalatestVer % "test",
)

// Print full stack traces in tests:
testOptions in Test += Tests.Argument("-oF")

// Assembly stuff (for fat jar)
mainClass in assembly := Some("com.example.kafkastreamsscalatemplate.Main")
assemblyJarName in assembly := "kafka-streams-scala-template.jar"

// Some stuff to import in the console
initialCommands in console := """

  // project stuff
  import com.example.kafkastreamsscalatemplate._
"""
