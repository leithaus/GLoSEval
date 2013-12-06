package com.biosimilarity.evaluator.util

import com.biosimilarity.evaluator.distribution._
import com.biosimilarity.evaluator.distribution.ConcreteHL._
import com.biosimilarity.evaluator.distribution.DSLCommLink.mTT
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._

class Slot(filter: CnxnCtxtLabel[String,String,String], connection: PortableAgentCnxn)
  extends EvaluationCommsService  
  with MessageGeneration
  with ChannelGeneration
  with EvalConfig
  with DSLCommLinkConfiguration
  with CnxnString[String,String,String]
  with StorageManagement
  with Serializable
{
  def read[T](success: Option[T] => Unit) = {
    BasicLogService.tweet("Slot.read")
    agentMgr().read(
      filter,
      List(connection),
      (optRsrc: Option[mTT.Resource]) => {
        BasicLogService.tweet("Slot.get | onGet: optRsrc = " + optRsrc)
        optRsrc match {
          case None => ()
          case Some(mTT.RBoundHM(Some(mTT.Ground( v )), _)) => v match {
            case Bottom => success(None)
            case PostedExpr( (PostedExpr( postedValue : T ), _, _) ) => success(Some(postedValue))
          }
          case v => throw new Exception("Unrecognized value: " + v)
        }
      }
    )
  }
  def get[T](success: Option[T] => Unit) = {
    BasicLogService.tweet("Slot.get")
    agentMgr().get(
      filter,
      List(connection),
      (optRsrc: Option[mTT.Resource]) => {
        BasicLogService.tweet("Slot.get | onGet: optRsrc = " + optRsrc)
        optRsrc match {
          case None => ()
          case Some(mTT.RBoundHM(Some(mTT.Ground( v )), _)) => v match {
            case Bottom => success(None)
            case PostedExpr( (PostedExpr( postedValue : T ), _, _) ) => success(Some(postedValue))
          }
          case v => throw new Exception("Unrecognized value: " + v)
        }
      }
    )
  }
  def put[T](value: T, k: () => Unit = () => ()) = {
    BasicLogService.tweet("Slot.put: value = " + value)
    agentMgr().put(
      filter,
      List(connection),
      (optRsrc: Option[mTT.Resource]) => {
        BasicLogService.tweet("Slot.put | onPut: optRsrc = " + optRsrc)
        optRsrc match {
          case None => ()
          case Some(_) => k()
        }
      }
    )
  }
}

object Usage {
  import com.biosimilarity.evaluator.distribution.PortableAgentCnxn
  import java.net.URI
  import com.biosimilarity.evaluator.distribution.usage.SimpleClient._
  
  def onPut() = println("onPut: done")
  def onGet[T](value: T) = println("onGet: value = " + value)
  def onRead[T](value: T) = println("onRead: value = " + value)
  
  val slot = new Slot(
    fromTermString("foo(_)").get,
    PortableAgentCnxn(new URI("a://b"), "flat", new URI("c://d"))
  )
}