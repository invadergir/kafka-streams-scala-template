package com.example.kafkastreamsscalatemplate

import java.util.Properties

import org.apache.kafka.common.serialization.{Serde, Serdes, StringDeserializer, StringSerializer}
import org.apache.kafka.streams.StreamsConfig.{APPLICATION_ID_CONFIG, BOOTSTRAP_SERVERS_CONFIG, DEFAULT_KEY_SERDE_CLASS_CONFIG, DEFAULT_VALUE_SERDE_CLASS_CONFIG}

object TestConstants {

  // instead of this, the class under test could expose its props probably.
  // (todo test)
  def getStreamConf = {
    val p = new Properties()
    p.put(APPLICATION_ID_CONFIG, "Test'App'")
    p.put(BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    p.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    p.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String.getClass.getName)
    p
  }

  val stringSer = new StringSerializer
  val stringDe = new StringDeserializer
  val stringSerde: Serde[String] = Serdes.String()

  val inputTopic = "inputtopic"
  val outputTopic = "outputtopic"

}

