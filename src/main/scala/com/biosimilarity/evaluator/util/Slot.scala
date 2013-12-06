package com.biosimilarity.evaluator.util

import com.biosimilarity.evaluator.distribution._
import com.biosimilarity.evaluator.distribution.ConcreteHL._
import com.biosimilarity.evaluator.distribution.DSLCommLink.mTT
import com.biosimilarity.lift.model.store._

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
    agentMgr().read(
      filter,
      List(connection),
      (optRsrc: Option[mTT.Resource]) => optRsrc match {
        case None => ()
        case Some(mTT.RBoundHM(Some(mTT.Ground( v )), _)) => v match {
          case Bottom => success(None)
          case PostedExpr( (PostedExpr( postedValue : T ), _, _) ) => success(Some(postedValue))
        }
        case v => throw new Exception("Unrecognized value: " + v)
      }
    )
  }
  def get[T](success: Option[T] => Unit) = {
    agentMgr().get(
      filter,
      List(connection),
      (optRsrc: Option[mTT.Resource]) => optRsrc match {
        case None => ()
        case Some(mTT.RBoundHM(Some(mTT.Ground( v )), _)) => v match {
          case Bottom => success(None)
          case PostedExpr( (PostedExpr( postedValue : T ), _, _) ) => success(Some(postedValue))
        }
        case v => throw new Exception("Unrecognized value: " + v)
      }
    )
  }
  def put[T](value: T, k: () => Unit = () => ()) = {
    agentMgr().put(
      filter,
      List(connection),
      (optRsrc: Option[mTT.Resource]) => optRsrc match {
        case None => ()
        case Some(_) => k()
      }
    )
  }
}
