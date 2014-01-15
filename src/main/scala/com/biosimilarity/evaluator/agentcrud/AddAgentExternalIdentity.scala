package com.biosimilarity.evaluator.agentcrud

import com.biosimilarity.lift.model.store._
import com.biosimilarity.evaluator.distribution.DSLCommLink.mTT
import com.biosimilarity.evaluator.distribution.ConcreteHL._
import com.biosimilarity.evaluator.distribution.usage.SimpleClient._
import com.biosimilarity.evaluator.util.Slot
import com.biosimilarity.evaluator.prolog.PrologDSL._

import javax.crypto._
import javax.crypto.spec.SecretKeySpec
import java.security._

import java.net.URI
import java.util.UUID

// Mask the json4s symbol2jvalue implicit so we can use the PrologDSL
object symbol2jvalue {}

case class AddAgentExternalIdentityError(reason: String) extends AgentCRUDResponse
case class AddAgentExternalIdentityResponse(sessionURI: URI) extends AgentCRUDResponse
case class AddAgentExternalIdentityWaiting(sessionURI: URI) extends AgentCRUDResponse

trait IDType
case class Email(address: String) extends IDType
case class ExternalIdentity(idType: IDType)

object URIs {
  val tokenToEmailURI = new URI("system://tokenToEmailMap")
  val emailToAgentURI = new URI("system://emailToAgentMap")
}

object ConfirmationEmail {
  def confirm(email: String, token: String) = {
    import org.apache.commons.mail._
    val simple = new SimpleEmail()
    simple.setHostName("smtp.googlemail.com")
    simple.setSmtpPort(465)
    simple.setAuthenticator(new DefaultAuthenticator("individualagenttech", "4genttech"))
    simple.setSSLOnConnect(true)
    simple.setFrom("individualagenttech@gmail.com")
    simple.setSubject("Confirm individual agent signup")
    // TODO(mike): get the URL from a config file
    simple.setMsg("Your token: " + token)
    simple.addTo(email)
    simple.send()
  }
}

case class AddAgentExternalIdentityRequest(sessionURI: URI, id: ExternalIdentity) extends AgentCRUDRequest {
  def handle(k: AgentCRUDResponse => Unit): Unit = {
    handle(k, confirmEmail)
  }
  def handle(
    k: AgentCRUDResponse => Unit, 
    confirm:(URI, String, AgentCRUDResponse => Unit) => Unit
  ): Unit = {
    id match {
      case ExternalIdentity(Email(address)) => {
        // Check if email is already registered in the system
        val emailTerm = 'email("address", "EncryptedAgentURI")
        val slot = new Slot(emailTerm, Conversion.selfConnection(URIs.emailToAgentURI))
        slot.read((opt: Option[Unit]) => opt match {
          // Does not exist; send confiramtion email
          case None => confirm(sessionURI, address, k)
          // Exists; error
          case _ => k(AddAgentExternalIdentityError("Email address is already taken."))
        })
      }
      case _ => {
        k(AddAgentExternalIdentityError("Unrecognized external identity type."))
      }
    }
  }
  
  def confirmEmail(sessionURI: URI, address: String, k: AgentCRUDResponse => Unit): Unit = {
    // Store (token -> address) in map
    // Then send email notification and tell user to check their email
    val token = UUID.randomUUID.toString.substring(0,8)
    val term = 'token(token, address)
    val slot = new Slot(term, Conversion.selfConnection(URIs.emailToAgentURI))
    slot.put(
      term,
      () => {
        ConfirmationEmail.confirm(address, token)
        k(AddAgentExternalIdentityWaiting(sessionURI))
      }
    )
  }
}

case class AddAgentExternalIdentityToken(sessionURI: URI, token: String) extends AgentCRUDRequest {
  import org.json4s._
  import org.json4s.native.JsonMethods._
  import org.json4s.JsonDSL._
  @transient
  implicit val formats = DefaultFormats

  def handle(k: AgentCRUDResponse => Unit): Unit = {
    val tokenTerm = 'token(token, "EmailAddress")
    agentMgr().get(
      tokenTerm,
      List(Conversion.selfConnection(URIs.tokenToEmailURI)), 
      (optRsrc: Option[mTT.Resource]) => optRsrc match {
        case None => ()
        case Some(mTT.RBoundHM(Some(mTT.Ground( v )), _)) => {
          v match {
            case Bottom => {
              k(AddAgentExternalIdentityError("No such token."))
            }
            case PostedExpr((
              PostedExpr(postedStr: String),
              filter: CnxnCtxtLabel[String,String,String],
              cnxn,
              bindings: mTT.RBoundAList
            )) => {
              // bindings.assoc is an Option[list of pairs]; turn it into a map, then pull out the expected key.
              val address = Map(bindings.assoc.get:_*).get("EmailAddress") match {
                case Some(CnxnCtxtLeaf(Left(str: String))) => str
                case _ => throw new Exception("EmailAddress variable not in bindings!")
              }
              // TODO(mike): check that a previous token wasn't already used
              addEmailToIdentities(sessionURI, address, k)
            }
          }
        }
        case _ => throw new Exception("Unrecognized resource: " + optRsrc)
      }
    )
  }

  def addEmailToIdentities(sessionURI: URI, address: String, k: AgentCRUDResponse => Unit): Unit = {
    val eiTerm = 'system('externalIdentities("Identities"))
    agentMgr().get(
      eiTerm,
      List(Conversion.sessionToAgent(sessionURI)),
      (optRsrc: Option[mTT.Resource]) => optRsrc match {
        case None => ()
        case Some(mTT.RBoundHM(Some(mTT.Ground( v )), _)) => {
          v match {
            case Bottom => {
              // create externalIdentities on Agent
              putIdentities(sessionURI, address, List(ExternalIdentity(Email(address))), k)
            }
            case PostedExpr((
              PostedExpr(postedStr: String),
              filter: CnxnCtxtLabel[String,String,String],
              cnxn,
              bindings: mTT.RBoundAList
            )) => {
              // Add email to externalIdentities, then put.
              // bindings.assoc is an Option[list of pairs]; turn it into a map, then pull out the expected key.
              val identities = Map(bindings.assoc.get:_*).get("Identities") match {
                case Some(CnxnCtxtLeaf(Left(str: String))) => str
                case _ => throw new Exception("Identities variable not in bindings!")
              }
              // convert from JSON to List[ExternalIdentities]
              // TODO(mike): allow extraction of more idTypes than just email
              val idList = parse(identities).asInstanceOf[JArray].arr.map(
                (id) => ExternalIdentity(Email((id \ "idType" \ "address").extract[String]))
              )
              putIdentities(sessionURI, address, idList :+ ExternalIdentity(Email(address)), k)
            }
          }
        }
        case _ => throw new Exception("Unrecognized resource: " + optRsrc)
      }
    )
  }
  
  def putIdentities(sessionURI: URI, address: String, ids: List[ExternalIdentity], k: AgentCRUDResponse => Unit): Unit = {
    import org.json4s.native.Serialization
    import org.json4s.native.Serialization.write
    val idsTerm = 'system('externalIdentities(write(ids)))
    agentMgr().put(
      idsTerm,
      List(Conversion.sessionToAgent(sessionURI)),
      idsTerm.toString(),
      (optRsrc: Option[mTT.Resource]) => optRsrc match {
        case None => ()
        case Some(_) => {
          // TODO(mike): encrypt the Agent URI with the user's key
          //    1. get the agent from the sessionURI
          //    2. look up aesK on agent self-connection with userPWDBLabel
          //    3. use user password to decrypt K and encrypt the agentURI with K
          //       This part is questionable; can we store the info to do that in the sessionURI?
          val emailTerm = 'email(address, "")
          agentMgr().put(
            emailTerm,
            List(Conversion.sessionToAgent(URIs.emailToAgentURI)),
            emailTerm.toString(),
            (optRsrc: Option[mTT.Resource]) => optRsrc match {
              case None => ()
              case Some(_) => {
                k(AddAgentExternalIdentityResponse(sessionURI))
              }
            }
          )
        }
      }
    )
  }
}
