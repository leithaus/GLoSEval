// -*- mode: Scala;-*- 
// Filename:    URIHandler.scala 
// Authors:     lgm                                                    
// Creation:    Wed Apr 30 17:55:35 2014 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.evaluator.spray

import com.biosimilarity.evaluator.distribution.portable.btc.v0_1._

import com.protegra_ati.agentservices.store._

import com.biosimilarity.evaluator.distribution._
import com.biosimilarity.evaluator.msgs._
import com.biosimilarity.lift.model.store._
import com.biosimilarity.lift.lib._

import akka.actor._
import spray.routing._
import directives.CompletionMagnet
import spray.httpx.encoding._

import spray.httpx.RequestBuilding._
import spray.http._
import spray.http.StatusCodes._
import MediaTypes._
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

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.typesafe.config._

import java.util.UUID

import java.net.URI
import java.net.URL

trait URIHandler extends URIHandlerT {
  self : DownStreamHttpCommsT with QryToURIT with CnxnString[String,String,String] =>
  import DSLCommLink.mTT
  import ConcreteHL._
  
  def formDataMap( uri : java.net.URI ) : Map[String,String] = {
    ( Map[String,String]( ) /: uri.getQuery.split( "&" ).map( _.split( "=" ) ) )(
      ( acc, e ) => {
        acc + ( e( 0 ) -> e( 1 ) )
      }
    )
  }

  def mkPosts(
    cnxn : URICnxn,
    filter : CnxnCtxtLabel[String,String,String]
  ) : List[( String, Int, HttpRequest )] = {
    queryToURIs( cnxn )( filter ).map(
      {
        ( uri : URI ) => {
          val port = 
            if ( uri.getPort < 0 ) { 80 } else { uri.getPort }
         ( uri.getHost, port, Post( uri.getPath, FormData( formDataMap( uri ) ) ) )
        }
      }
    )
  }

  def mkFutures(
    cnxn : URICnxn,
    filter : CnxnCtxtLabel[String,String,String]
  ) : List[Future[HttpResponse]] = {
    import concurrent.ExecutionContext.Implicits._
    
    implicit val system = ActorSystem()
    val ioBridge = IOExtension(system).ioBridge()
    val httpClient = system.actorOf(Props(new HttpClient(ioBridge)))
    
    for( (host, port, req ) <- mkPosts( cnxn, filter ) ) yield {
      val conduit = system.actorOf(
        props = Props(new HttpConduit(httpClient, host, port)),
        name = "http-conduit"
      )
      
      val pipeline = HttpConduit.sendReceive(conduit)

      pipeline(req)
    }    
  }

  def resolveFutures(
    futures : List[Future[HttpResponse]],
    cnxn : PortableAgentCnxn,
    label : CnxnCtxtLabel[String,String,String],
    onPost : Option[mTT.Resource] => Unit = {
      ( optRsrc : Option[mTT.Resource] ) => BasicLogService.tweet( "post response: " + optRsrc )
    },
    dslEvaluator : EvaluationCommsService = EvalHandlerService
  ) : Unit = {
    for( response <- futures ) {
      response onComplete{
        case Failure( ex ) => ex.printStackTrace()
        case Success( rsp ) => {
          val rspData = rsp.entity.asString
          //println( "HTTP response to " + rq.url + rq.data + " is " + resp.toString )
          println( "response data" +  " is " + rspData )
          println(
            (
              "*********************************************************************************"
              + "\nputting response data "
              + "\ncnxn: " + cnxn
              + "\nlabel: " + label
              + "\nrspData: " + rspData
              + "\n*********************************************************************************"
            )
          )
          dslEvaluator.put( label, List( cnxn ), rspData, onPost )
        }
      }
    }
  }

  def ask(
    qryCnxn : URICnxn,
    filter : CnxnCtxtLabel[String,String,String],
    rspCnxn : PortableAgentCnxn,
    label : CnxnCtxtLabel[String,String,String],
    onPost : Option[mTT.Resource] => Unit = {
      ( optRsrc : Option[mTT.Resource] ) => BasicLogService.tweet( "post response: " + optRsrc )
    },
    dslEvaluator : EvaluationCommsService = EvalHandlerService
  ) {    
    resolveFutures(
      mkFutures( qryCnxn, filter ),
      rspCnxn, label, onPost,
      dslEvaluator
    )
  }

  // BUGBUG : lgm -- none of the write methods are implemented and
  // most of the read methods are all the same

  override def post[Value](
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    content : Value,
    onPost : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    throw new Exception( "Not yet implemented" )
  }

  override def postV[Value](
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    content : Value,
    onPost : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    throw new Exception( "Not yet implemented" )    
  }

  override def put[Value](
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    content : Value,
    onPut : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    throw new Exception( "Not yet implemented" )        
  }

  override def read(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onReadRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) },
    sequenceSubscription : Boolean = false
  ) : Unit = {
    for( uriCnxn <- cnxns ) {
      val rspCnxn =
        PortableAgentCnxn( uriCnxn.src, uriCnxn.label, uriCnxn.trgt )
      val label =
        fromTermString( s"""httpRsp( ${filter} )""" ).get

      ask(
        uriCnxn, filter,
        rspCnxn, label,
        onReadRslt
      )
    }
  }

  override def fetch(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onFetchRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    for( uriCnxn <- cnxns ) {
      val rspCnxn =
        PortableAgentCnxn( uriCnxn.src, uriCnxn.label, uriCnxn.trgt )
      val label =
        fromTermString( s"""httpRsp( ${filter} )""" ).get

      ask(
        uriCnxn, filter,
        rspCnxn, label,
        onFetchRslt
      )
    }    
  }

  override def feed(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onFeedRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    for( uriCnxn <- cnxns ) {
      val rspCnxn =
        PortableAgentCnxn( uriCnxn.src, uriCnxn.label, uriCnxn.trgt )
      val label =
        fromTermString( s"""httpRsp( ${filter} )""" ).get

      ask(
        uriCnxn, filter,
        rspCnxn, label,
        onFeedRslt
      )
    }    
  }

  override def get(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    onGetRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    for( uriCnxn <- cnxns ) {
      val rspCnxn =
        PortableAgentCnxn( uriCnxn.src, uriCnxn.label, uriCnxn.trgt )
      val label =
        fromTermString( s"""httpRsp( ${filter} )""" ).get

      ask(
        uriCnxn, filter,
        rspCnxn, label,
        onGetRslt
      )
    }    
  }

  override def score(
    filter : CnxnCtxtLabel[String,String,String],
    cnxns : Seq[URICnxn],
    staff : Either[Seq[Cnxn],Seq[Label]],
    onScoreRslt : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "got response: " + optRsrc ) }
  ) : Unit = {
    for( uriCnxn <- cnxns ) {
      val rspCnxn =
        PortableAgentCnxn( uriCnxn.src, uriCnxn.label, uriCnxn.trgt )
      val label =
        fromTermString( s"""httpRsp( ${filter} )""" ).get

      ask(
        uriCnxn, filter,
        rspCnxn, label,
        onScoreRslt
      )
    }    
  }

  override def cancel(
    filter : CnxnCtxtLabel[String,String,String],
    connections : Seq[URICnxn],
    onCancel : Option[Rsrc] => Unit =
      ( optRsrc : Option[Rsrc] ) => { BasicLogService.tweet( "onCancel: optRsrc = " + optRsrc ) }
  ) : Unit = {
    throw new Exception( "Not yet implemented" )    
  }
}
