package com.example.kafkastreamsscalatemplate

import java.lang

import org.scalatest.junit.JUnitRunner
import com.madewithtea.mockedstreams.MockedStreams
import org.apache.kafka.common.serialization.Serdes

import scala.collection.immutable

class StreamProcessorSpec extends UnitSpec {

  // before all tests have run
  override def beforeAll() = {
    super.beforeAll()
  }

  // before each test has run
  override def beforeEach() = {
    super.beforeEach()
  }

  // after each test has run
  override def afterEach() = {
    //myAfterEach()
    super.afterEach()
  }

  // after all tests have run
  override def afterAll() = {
    super.afterAll()
  }

  // Import test constants with topic names and mock kafka setup, etc.
  import TestConstants._

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  // Tests start
  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  describe("StreamProcessor") 
  {
    it("should split input objects into two objects") {

      val input = Seq("A"->"1", "B"->"2", "C"->"3")

      val expectedOutput = Seq("A"->"1", "A"->"1X", "B"->"2", "B"->"2X", "C"->"3", "C"->"3X")

      val result = MockedStreams().topology { builder =>
        StreamProcessor.createTopology(builder, inputTopic, outputTopic)
      }
        .config(getStreamConf)  // returns MockedStreams.Builder
        .input(inputTopic, stringSerde, stringSerde, input) // returns MockedStreams.Builder
        .output(outputTopic, stringSerde, stringSerde, expectedOutput.size)

      println("result = "+result)
      result should be (expectedOutput)
    }

    it("Java-style topology should also split input objects into two objects - feel free to delete this...") {

      val input = Seq("A"->"1", "B"->"2", "C"->"3")

      val expectedOutput = Seq("A"->"1", "A"->"1X", "B"->"2", "B"->"2X", "C"->"3", "C"->"3X")

      val result = MockedStreams().topology { builder =>
        StreamProcessor.createTopologyJava(builder, inputTopic, outputTopic)
      }
        .config(getStreamConf)  // returns MockedStreams.Builder
        .input(inputTopic, stringSerde, stringSerde, input) // returns MockedStreams.Builder
        .output(outputTopic, stringSerde, stringSerde, expectedOutput.size)

      println("result = "+result)
      result should be (expectedOutput)
    }
  }
}

  

