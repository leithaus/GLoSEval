package com.biosimilarity.evaluator.agentcrud

import com.biosimilarity.evaluator.distribution._
// import com.protegra_ati.agentservices.store._
import com.biosimilarity.lift.model.store._
// import com.biosimilarity.lift.lib._

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.JsonDSL._

trait AgentCRUDResponse

trait AgentCRUDRequest
  extends EvaluationCommsService  
  with MessageGeneration
  with ChannelGeneration
  with EvalConfig
  with DSLCommLinkConfiguration
  with CnxnString[String,String,String]
  with StorageManagement
  with Serializable
{
  def handle(k: AgentCRUDResponse => Unit): Unit
}

object Conversion {
  import java.net.URI
  import com.biosimilarity.evaluator.distribution._
  
  def selfConnection(uri: URI): PortableAgentCnxn = {
    PortableAgentCnxn(
      uri,
      "identity",
      uri
    )
  }
  
  def sessionToAgent(sessionURI: URI): PortableAgentCnxn = {
    selfConnection(new URI(
      "agent",
      sessionURI.getHost(),
      null,
      null
    ))
  }
}
