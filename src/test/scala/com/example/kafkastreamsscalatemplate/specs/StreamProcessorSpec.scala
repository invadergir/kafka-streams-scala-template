package com.example.kafkastreamsscalatemplate

import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.Topology
import com.madewithtea.mockedstreams.MockedStreams

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

  def testTopology(
    input: Seq[(String, String)], 
    expectedOutput: Seq[(String, String)],
    createTopologyFn: (StreamsBuilder, String, String) => Topology
  ): Unit = {

    val result = MockedStreams().topology { builder =>
      createTopologyFn(builder, inputTopic, outputTopic)
    }
      .config(getStreamConf)  // returns MockedStreams.Builder
      .input(inputTopic, stringSerde, stringSerde, input) // returns MockedStreams.Builder
      .output(outputTopic, stringSerde, stringSerde, expectedOutput.size)

    println("result = "+result)
    result should be (expectedOutput)
  }

  describe("StreamProcessor") 
  {
    it("when value is non-json, it should split input objects into two objects and add 'XXX'") {

      val input = Seq("A"->"1", "B"->"2", "C"->"3", "D"->"""{"D": "not json}""")
      val expectedOutput = Seq(
        "A"->"1", 
        "A"->"1XXX", 
        "B"->"2", 
        "B"->"2XXX", 
        "C"->"3", 
        "C"->"3XXX", 
        "D"->"""{"D": "not json}""",
        "D"->"""{"D": "not json}XXX"""
      )
      testTopology(input, expectedOutput, StreamProcessor.createTopology)
    }

    it("when input value is parseable to json, it should split the json object into two objects and add 'X':'XXX'") {

      val input = Seq("A"->"""{"A":"AAA"}""", "B"->"""{"B":"BBB"}""")
      val expectedOutput = Seq("A"->"""{"A":"AAA"}""", "A"->"""{"A":"AAA","X":"XXX"}""", "B"->"""{"B":"BBB"}""", "B"->"""{"B":"BBB","X":"XXX"}""")
      testTopology(input, expectedOutput, StreamProcessor.createTopology)
    }
  }

//  describe("StreamProcessor - java Topology - feel free to delete this...")
//  {
//    it("when value is non-json, it should split input objects into two objects and add 'XXX'") {
//
//      val input = Seq("A"->"1", "B"->"2", "C"->"3", "D"->"""{"D": "not json}""")
//      val expectedOutput = Seq(
//        "A"->"1",
//        "A"->"1XXX",
//        "B"->"2",
//        "B"->"2XXX",
//        "C"->"3",
//        "C"->"3XXX",
//        "D"->"""{"D": "not json}""",
//        "D"->"""{"D": "not json}XXX"""
//      )
//      testTopology(input, expectedOutput, StreamProcessor.createTopologyJava)
//    }
//
//    it("when input value is parseable to json, it should split the json object into two objects and add 'X':'XXX'") {
//
//      val input = Seq("A"->"""{"A":"AAA"}""", "B"->"""{"B":"BBB"}""")
//      val expectedOutput = Seq("A"->"""{"A":"AAA"}""", "A"->"""{"A":"AAA","X":"XXX"}""", "B"->"""{"B":"BBB"}""", "B"->"""{"B":"BBB","X":"XXX"}""")
//      testTopology(input, expectedOutput, StreamProcessor.createTopologyJava)
//    }
//  }
}

  

