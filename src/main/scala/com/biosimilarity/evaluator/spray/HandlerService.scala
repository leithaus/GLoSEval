// -*- mode: Scala;-*- 
// Filename:    HandlerService.scala 
// Authors:     lgm                                                    
// Creation:    Wed Oct  2 17:51:11 2013 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.evaluator.spray

import com.protegra_ati.agentservices.store._

import com.biosimilarity.evaluator.distribution._
import com.biosimilarity.evaluator.msgs._
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._

import akka.actor._
import spray.routing._
import directives.CompletionMagnet
import spray.http._
import spray.http.StatusCodes._
import MediaTypes._

import spray.httpx.encoding._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.continuations._ 
import scala.collection.mutable.HashMap

import com.typesafe.config._

import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import java.security._


import java.util.Date
import java.util.UUID

import java.net.URI

object URIHandlerService
extends URIHandler
with QryToURIT
with DownStreamHttpCommsT
//with EvaluationCommsService
//with EvalConfig
//with DSLCommLinkConfiguration
//with AccordionConfiguration
with EvaluationServiceProxy
with CnxnString[String,String,String]
with Serializable {
  override def evaluationService = EvalHandlerService
}

object EvalHandlerService
extends EvalHandler
with EvaluationCommsService
with EvalConfig
with DSLCommLinkConfiguration
with AccordionConfiguration
with DownStreamHttpCommsT
with EvaluationServiceProxy
with QryToURIT
with BTCHandler
with Serializable {
  override def uriHandler() : URIHandlerT = URIHandlerService
  override def evaluationService = this
}

object EvalAndAgentCRUDHandlerService
extends EvalHandler
with AgentCRUDHandler
with EvaluationCommsService
with EvalConfig
with DSLCommLinkConfiguration
with AccordionConfiguration
with DownStreamHttpCommsT
with EvaluationServiceProxy
with QryToURIT
with BTCHandler
with Serializable {
  override def uriHandler() : URIHandlerT = URIHandlerService
  override def evaluationService = this
}

package usage {
  object EvalHandlerHandler {
    val sampleCreateUserRequest : JValue =
      parse("""{"msgType":"createUserRequest","content":{"email":"metaweta+1@gmail.com","password":"4gent","jsonBlob":{"name":"Agent 007"}}}""")
  }
}
