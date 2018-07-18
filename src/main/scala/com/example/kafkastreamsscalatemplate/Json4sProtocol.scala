package com.example.kafkastreamsscalatemplate

import org.json4s._
import org.json4s.ext.{EnumNameSerializer, JavaTimeSerializers}

object Json4sProtocol {
  implicit val formats = 

    // Need to do this to get milliseconds. 
    // Note that this assumes LocalDateTime, which is itself assumed UTC.
    org.json4s.DefaultFormats ++
    // write nulls rather than skip for Nones:
    //.preservingEmptyValues ++
    // Need these for serializing LocalDateTime, etc.:
    JavaTimeSerializers.all //+
    // Enums have to be listed individually:
    //new EnumNameSerializer(MyEnum) + 
}



