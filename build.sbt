name := "kafka-streams-scala-template"

version := "0.1"

organization := "com.example"

scalaVersion := "2.13.8"

// Always fork the jvm (test and run)
fork := true

// Allow CTRL-C to cancel running tasks without exiting SBT CLI.
Global / cancelable := true

val json4SVer = "3.6.12"
val scalatestVer = "3.2.12"
val kafkaVer = "2.7.2"
val mockedstreamsVer = "3.9.0" // compat with kafka 2.7.0

libraryDependencies ++= Seq(

  // Kafka streams
  "org.apache.kafka" % "kafka-clients" % kafkaVer,
  "org.apache.kafka" % "kafka-streams" % kafkaVer,
  "org.apache.kafka" %% "kafka-streams-scala" % kafkaVer,

  // For JSON parsing (see https://github.com/json4s/json4s)
  "org.json4s" %%  "json4s-jackson" % json4SVer,
  "org.json4s" %%  "json4s-ext" % json4SVer,

  // config
  "com.github.pureconfig" %% "pureconfig" % "0.17.1",

  // logging
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  
  // For testing:
  "org.scalatest" %% "scalatest" % scalatestVer % Test,
  "org.scalactic" %% "scalactic" % scalatestVer % Test,
  "com.madewithtea" %% "mockedstreams" % mockedstreamsVer % Test,
)

// Print full stack traces in tests:
Test / testOptions += Tests.Argument("-oF")

// Assembly stuff (for fat jar)
assembly / mainClass := Some("com.example.kafkastreamsscalatemplate.Main")
assembly / assemblyJarName := "kafka-streams-scala-template.jar"

// Some stuff to import in the console
console / initialCommands := """

  // project stuff
  import com.example.kafkastreamsscalatemplate._
"""

