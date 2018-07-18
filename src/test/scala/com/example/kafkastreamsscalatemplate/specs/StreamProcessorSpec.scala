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
    it("should map input to output") {

      // This stream processor doesn't really do much
      // except store and output the input (its a pipe).
      // So there's not much to test.

      val input = Seq("A"->"1", "B"->"2", "C"->"3")

      val expectedOutput = Seq("A"->"1", "B"->"2", "C"->"3")

      val result = MockedStreams().topology { builder =>
        StreamProcessor.createTopology(builder, inputTopic, outputTopic)
      }
        .config(getStreamConf)  // returns MockedStreams.Builder
        .input(inputTopic, stringSerde, stringSerde, input) // returns MockedStreams.Builder
        .output(outputTopic, stringSerde, stringSerde, input.size)

      println("result = "+result)
      result should be (expectedOutput)
    }
  }
}

  

