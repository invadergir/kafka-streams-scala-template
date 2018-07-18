package com.example.kafkastreamsscalatemplate

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.util.StatusPrinter
import com.typesafe.scalalogging.StrictLogging
import org.slf4j.LoggerFactory

object Main 
  extends App
  with StrictLogging 
{

  // print logback's internal status
  StatusPrinter.print(LoggerFactory.getILoggerFactory().asInstanceOf[LoggerContext])

  logger.info("Starting stream processor...")
  
  StreamProcessor.run()
  
  logger.info("Stream processor done.")
}
