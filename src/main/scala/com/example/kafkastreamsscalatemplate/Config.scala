package com.example.kafkastreamsscalatemplate

import pureconfig._
import pureconfig.configurable._
import util.ValidString

// app-wide settings / configuration object
object Config {

  // This allows you to use camel case in the config (provides easier searching when 
  // the names are the same):
  implicit def productHint[T] = ProductHint[T](ConfigFieldMapping(CamelCase, CamelCase))
  
  // How to do enums in the config:
  //implicit val myEnumExampleType = ConfigReader.fromString[MyEnumExample.Value](
  //  ConvertHelpers.catchReadError(s => MyEnumExample.withName(s)))
  //implicit val converterOffsetDateTime = offsetDateTimeConfigConvert(DateTimeFormatter.ISO_DATE_TIME)
  //implicit val converterlocalDateTime = localDateTimeConfigConvert(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

  // global app configuration
  val app: AppConfig = pureconfig.loadConfigOrThrow[AppConfig]("kafkastreamsscalatemplate")
  app.validate()
  println(s"App Configuration is $app")
  
  /** Helper method to check a boolean and throw if not true.
    * 
    */
  def check(boolean: Boolean, errorMessage: String = "Configuration Error!"): Unit = {
    if ( !boolean ) {
      throw new RuntimeException(errorMessage)
    }
  }
}

import Config.check

// top-level config object
case class AppConfig(kafka: KafkaConfig) {
  def validate() = kafka.validate()
}

case class KafkaConfig(
  server: ServerConnection,
  inputTopic: String,
  outputTopic: String,
) {

  /** validate
    */
  def validate() = {
    server.validate()
    check(ValidString(inputTopic).nonEmpty)
    check(ValidString(outputTopic).nonEmpty)
  }
}

case class ServerConnection(
  host: String,
  port: Int,
  protocol: String = "http",
) {

  /**
   * Simple check on the connection parameters. 
   */
  def validate(): Unit = { 
    check(host.nonEmpty && port > 0 && protocol.nonEmpty, 
      "ServerConnection instance is invalid: "+this.toString)
  }

  /** Get the host and port for use in connection strings.
    * 
    */
  val hostPort: String = {
    host + ":" + port.toString
  }
}

