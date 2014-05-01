// -*- mode: Scala;-*- 
// Filename:    EvaluationServiceProxy.scala 
// Authors:     lgm                                                    
// Creation:    Thu May  1 14:23:27 2014 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.evaluator.spray

import com.protegra_ati.agentservices.store._

import com.biosimilarity.evaluator.distribution._
import com.biosimilarity.evaluator.msgs._
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._

import spray.httpx.RequestBuilding._
import spray.http._
import HttpMethods._
import HttpHeaders._
import MediaTypes._
import spray.json._
import DefaultJsonProtocol._
import spray.httpx.SprayJsonSupport._
import akka.actor.ActorSystem
import spray.io.IOExtension
import akka.actor.Props
import spray.can.client.HttpClient
import spray.client.HttpConduit
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success

import java.net.URL

trait EvaluationServiceProxy {
  def evaluationService : EvaluationService
}
