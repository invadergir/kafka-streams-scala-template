package com.example.kafkastreamsscalatemplate

import java.util.Properties
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.lightbend.kafka.scala.streams.{KStreamS, StreamsBuilderS}
import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams._
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}
import org.json4s.JValue
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}

import scala.collection.JavaConverters._
import scala.util.Try

object StreamProcessor extends StrictLogging{

  val appName = "StreamProcessor"
  val inputTopic = Config.app.kafka.inputTopic
  val outputTopic = Config.app.kafka.outputTopic

  // Formatter for json4s
  import Json4sProtocol.formats

  // runs a stream and safely shuts down
  val latch: CountDownLatch = new CountDownLatch(1)
  def runStreamAndShutdown(stream: KafkaStreams) = {

    Runtime.getRuntime.addShutdownHook(new Thread(() => {
      logger.info(">>>>>> Closing stream...")
      stream.close(10, TimeUnit.SECONDS)
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
  
    // Start the stream by creating a wrapped StreamsBuilder.
    val builderS = new StreamsBuilderS(builder)

    // Create the input stream.
    // (Need an implicit Consumed object.)
    implicit val consumed =
      Consumed.`with`(Serdes.String, Serdes.String)
    val source: KStreamS[String, String] = builderS.stream(inputTopic)

    // produce 2 messages for every 1 input message.
    val outStream = source.flatMap{ (key, value) =>
      transform(key, value)
    }

    // Output to the output topic.  (Need an implicit Produced.)
    implicit val produced = Produced.`with`(Serdes.String, Serdes.String)
    outStream.to(outputTopic)

    // Create the topology
    val topology: Topology = builderS.build()
    logger.info(">>>>  topology = "+topology.describe)
    topology
  }

  /** Create the stream topology, using the raw Java
    * API.  Not sure why you'd want to use this though.  Included 
    * is a flatMap implementation for comparison to the above. 
    * Note the extra boilerplate that is removed.  The same 
    * transformation is applied to the input as in the Scala 
    * topology above. 
    */
  def createTopologyJava(
    builder: StreamsBuilder,
    inputTopic: String,
    outputTopic: String
  ): Topology = {
  
    // start the stream
    val source: KStream[String, String] = builder.stream(inputTopic)

    // Flatmap example using Java API: produce 2 messages for every 1 input message.  
    val outStream: KStream[String, String] =
      source.flatMap(new KeyValueMapper[String, String, java.lang.Iterable[KeyValue[String, String]]] {
        def apply(key: String, value: String): java.lang.Iterable[KeyValue[String, String]] = {
          transform(key, value).map{ case (k, v) => 
            new KeyValue[String, String](k, v),
          }.asJava
        }
      })
    
    // output to the output topic
    outStream.to(outputTopic)

    // Create the topology
    val topology: Topology = builder.build()
    logger.info(">>>>  topology = "+topology.describe)
    topology
  }

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
        (key, value),
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
      p.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass)
      p.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass)

      // attempt to speed it up
      p.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, new Integer(0))
      p.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, new Integer(2 * 1000))
      p
    }

    // create the builder and topology
    val builder: StreamsBuilder = new StreamsBuilder()
    val topology = createTopology(builder, inputTopic, outputTopic)
    logger.info(">>> >>>")
    logger.info(">>> >>> topology = {}", topology.describe)

    // Create the streams object from the topology
    val streams: KafkaStreams = new KafkaStreams(topology, config)

    // now run 
    runStreamAndShutdown(streams)
  }

}
