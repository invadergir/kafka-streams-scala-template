package com.example.kafkastreamsscalatemplate

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatest.{FunSpec, Inspectors, Matchers}

abstract class UnitSpec 
  extends FunSpec 
  with Matchers 
  with BeforeAndAfterAll
  with BeforeAndAfterEach
  with TypeCheckedTripleEquals 
  with Inspectors


