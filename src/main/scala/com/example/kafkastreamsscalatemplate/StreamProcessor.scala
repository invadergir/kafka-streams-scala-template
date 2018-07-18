package com.example.kafkastreamsscalatemplate

import java.util.Properties
import java.util.concurrent.{CountDownLatch, TimeUnit}

import com.typesafe.scalalogging.StrictLogging
import org.apache.kafka.common.serialization._
import org.apache.kafka.streams.kstream._
import org.apache.kafka.streams.{KafkaStreams, StreamsBuilder, StreamsConfig, Topology}
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization.{read, write}


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
  
    // start the stream
    val source: KStream[String, String] = builder.stream(inputTopic)

    // todo process the data...
    
    // output to the output topic
    source.to(outputTopic)

    // Create the topology
    val topology: Topology = builder.build()
    logger.info(">>>>  topology = "+topology.describe)
    topology
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
