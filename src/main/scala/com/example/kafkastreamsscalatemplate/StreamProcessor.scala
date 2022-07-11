package com.example.kafkastreamsscalatemplate

import com.typesafe.scalalogging.StrictLogging

import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.Properties

import org.apache.kafka.streams.kstream.{KeyValueMapper, Materialized}
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.kstream._
import org.apache.kafka.streams.scala.serialization.Serdes
import org.apache.kafka.streams.{KafkaStreams, KeyValue, StreamsConfig, Topology}
import Serdes._

import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.JValue

import scala.util.Try

//import scala.jdk.CollectionConverters._

object StreamProcessor extends StrictLogging{

  val appName = "StreamProcessor"

  // runs a stream and safely shuts down
  val latch: CountDownLatch = new CountDownLatch(1)
  def runStreamAndShutdown(stream: KafkaStreams) = {

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      logger.info(">>>>>> Closing stream...")
      stream.close(Duration.ofSeconds(10))
      logger.info(">>>>>> Done closing stream.")
      latch.countDown()
    }))

    try {
      stream.start()
      latch.await()
    } catch {
      case e: Exception =>
        e.printStackTrace()
        System.exit(1)
    }
    System.exit(0)
  }
  
  /** Create the stream topology. 
    * Separating this logic makes testing easier.
    */
  def createTopology(
    builder: StreamsBuilder,
    inputTopic: String,
    outputTopic: String
  ): Topology = {
  
    // Create the input stream.
    val source: KStream[String, String] = builder.stream(inputTopic)

    // produce 2 messages for every 1 input message.
    val outStream = source.flatMap{ (key, value) =>
      transform(key, value)
    }

    // Output to the output topic.
    outStream.to(outputTopic)

    // Create the topology
    val topology: Topology = builder.build()
    logger.info(">>>>  topology = "+topology.describe)
    topology
  }

  /** Create the stream topology, using the raw Java
    * API, included for comparison to the above.
    * Note the extra boilerplate that is removed.  The same 
    * transformation is applied to the input as in the Scala 
    * topology above. 
    */
//  def createTopologyJava(
//    builder: StreamsBuilder,
//    inputTopic: String,
//    outputTopic: String
//  ): Topology = {
//
//    // start the stream
//    val source: KStream[String, String] = builder.stream(inputTopic)
//
//    // Flatmap example using Java API: produce 2 messages for every 1 input message.
//    val outStream: KStream[String, String] =
//      source.flatMap(new KeyValueMapper[String, String, java.lang.Iterable[KeyValue[String, String]]] {
//        def apply(key: String, value: String): java.lang.Iterable[KeyValue[String, String]] = {
//          transform(key, value).map{ case (k, v) =>
//            new KeyValue[String, String](k, v),
//          }.asJava
//        }
//      })
//
//    // output to the output topic
//    outStream.to(outputTopic)
//
//    // Create the topology
//    val topology: Topology = builder.build()
//    logger.info(">>>>  topology = "+topology.describe)
//    topology
//  }

  /**
    * Transform the input message by splitting into two and 
    * modifying the second one. 
    *  
    * On the second message, add a k-v pair X:XXX if it is JSON, 
    * and if it is not JSON, append "XXX" to the end of the input 
    * string.  The keys are kept the same.
    * 
    * @return Key-value pairs in a List[(String, String)]
    */
  def transform(key: String, value: String): List[(String, String)] = {

    val parsed: Option[JValue] = value match {
      case s: String =>
        if (s.nonEmpty) {
          val firstChar = s.trim.substring(0,1)
          if (firstChar == "{" || firstChar == "[") {
            Try{parse(value)}.toOption
          }
          else None
        }
        else None
      case _ => None
    }

    if (parsed.isDefined) {
      List(
        (key, compact(parsed.get)),
        (key, compact(parsed.get merge parse("""{"X":"XXX"}""")))
      )
    }
    else {
      List(
        (key, value),
        (key, value + "XXX")
      )
    }
  }


  /** Main method
    */
  def run() {

    val config: Properties = {
      val p = new Properties()
      p.put(StreamsConfig.APPLICATION_ID_CONFIG, appName)
      p.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Config.app.kafka.server.hostPort)
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass.getName)
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.stringSerde.getClass.getName)

      // speed it up to aid testing.  In production, you should adjust these values.
      p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0)
      p.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 2 * 1000)
      p
    }

    // create the builder and topology
    val builder: StreamsBuilder = new StreamsBuilder()
    val inputTopic = Config.app.kafka.inputTopic
    val outputTopic = Config.app.kafka.outputTopic
    val topology = createTopology(builder, inputTopic, outputTopic)
    logger.info(">>> >>>")
    logger.info(">>> >>> topology = {}", topology.describe)

    // Create the streams object from the topology
    val streams: KafkaStreams = new KafkaStreams(topology, config)

    // now run 
    runStreamAndShutdown(streams)
  }

}
