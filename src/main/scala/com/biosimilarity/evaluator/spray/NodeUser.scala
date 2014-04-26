package com.biosimilarity.evaluator.spray

import com.biosimilarity.evaluator.distribution._

trait NodeUserConfiguration {
  self : EvalConfig =>
  var _nodeUserCfgObj : Option[com.typesafe.config.ConfigObject] = None
  def nodeUserCnfObj( configFileName : String = "eval.conf" ) : com.typesafe.config.ConfigObject = {
    _nodeUserCfgObj match {
      case Some( nUCO ) => nUCO 
      case None => {
        evalConfig( configFileName ).getObject( "NodeUser" )
      }
    }
  }
  def nodeUserImgFileName( configFileName : String = "eval.conf" ) : String = {
    try {
      nodeUserCnfObj( configFileName ).toConfig.getString( "imgFileName" )
    }
    catch {
      case e : Throwable => 
        "./src/main/resources/media/queenbee64.txt"
    }
  }
  def nodeUserImgSrc( configFileName : String = "eval.conf" ) : String = {
    io.Source.fromFile( nodeUserImgFileName( configFileName ) ).getLines.mkString
  }
  def nodeUserEmail( configFileName : String = "eval.conf" ) : String = {
    try {
      nodeUserCnfObj( configFileName ).toConfig.getString( "email" )
    }
    catch {
      case e : Throwable => 
        "splicious-design-team@googlegroups.com"
    }
  }
  def nodeUserPassword( configFileName : String = "eval.conf" ) : String = {
    try {
      nodeUserCnfObj( configFileName ).toConfig.getString( "password" )
    }
    catch {
      case e : Throwable => 
        "splicious"
    }
  }
  def nodeUserName( configFileName : String = "eval.conf" ) : String = {
    try {
      nodeUserCnfObj( configFileName ).toConfig.getString( "name" )
    }
    catch {
      case e : Throwable => 
        "Queen Splicious"
    }
  }
  def nodeUserJsonBlob( configFileName : String = "eval.conf" ) : String = {
    s"""{"name":"${nodeUserName( configFileName )}","imgSrc":"${nodeUserImgSrc( configFileName )}"}"""
  }
  def nodeUserWIFKey( configFileName : String = "eval.conf" ) : String = {
    try {
      nodeUserCnfObj( configFileName ).toConfig.getString( "WIFKey" )
    }
    catch {
      case e : Throwable => 
        "5HyhVtjqRi2c91ftvCBLwMEfjg2HG3WcRkfYyPatvg8hg2vmhSY"
    }
  }
}

object NodeUser extends EvalConfig with NodeUserConfiguration {
  val imgFileName = nodeUserImgFileName()
  val imgSrc = nodeUserImgSrc()
  val email = nodeUserEmail()
  val password = nodeUserPassword()
  val jsonBlob = nodeUserJsonBlob()
  val wifKey = nodeUserWIFKey()
}
