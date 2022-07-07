# Template / Starter App for Kafka Streams

## Goals

To provide a starter project for Kafka Streams with Scala.  It includes:
* Simple working stream processor with both Scala-style and Java-style flatMap examples, ready to be added to.
* Logging with scala-logging
* unit-testing of streams via mockedstreams
* Json handling with Json4s
* Fat jar support via assembly plugin

## To Use

### Run

If you run this project, the example behavior is to transform the input messages on the "input" topic in the following way:

1. Copy input message into two
1. The first copy is unchanged.
1. The 2nd copy is modified.  For messages that parse into JSON, add "X": "XXX" to it.  For non-JSON, simply append "XXX" to the end.

The transformed messages are output onto the "output" topic.

This project also can be used to test robot framework kafka stream testing applications.  See [https://github.com/invadergir/robotframework-pykafka](https://github.com/invadergir/robotframework-pykafka) for an example for testing this app in robot framework.

To build and run the project (must have [sbt](https://www.scala-sbt.org/download.html) installed):
```
sbt test run
```

Use your favorite kafka producer and consumer to send it text strings and/or JSON strings to see what it does on the output topic.  For example:

```
kafka-console-producer.sh --broker-list localhost:9092 --property parse.key=true --property 'key.separator=~~' --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --topic input
>A~~1
>B~~2
```

The output should be:

```
+ kafka-console-consumer.sh --bootstrap-server localhost:9092 --formatter kafka.tools.DefaultMessageFormatter --property print.key=true --property print.value=true --property key.deserializer=org.apache.kafka.common.serialization.StringDeserializer --property value.deserializer=org.apache.kafka.common.serialization.StringDeserializer --topic output
A       1
A       1XXX
B       2
B       2XXX
```

### Build Jar

```
sbt assembly
```

### To Rename Project 

To use this as a starter project, you can run the 'renameproject.sh' script as follows:

```
./renameproject.sh new-project-name -p $NEW_PACKAGE_PREFIX
```
...where NEW_PACKAGE_PREFIX is "com.mycompany.myproject" or something like that.

For help running the script, please type: 

```
./renameproject.sh -h
```
