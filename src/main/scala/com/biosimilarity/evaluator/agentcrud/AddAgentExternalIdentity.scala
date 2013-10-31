package com.biosimilarity.evaluator.agentcrud
import java.net.URI

case class AddAgentExternalIdentityError(reason: String) extends AgentCRUDResponse
case class AddAgentExternalIdentityResponse(sessionURI: URI) extends AgentCRUDResponse
case class AddAgentExternalIdentityWaiting(sessionURI: URI) extends AgentCRUDResponse

case class IDType
case class Email(address: String) extends IDType
case class ExternalIdentity(idType: IDType)

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
  var emailToAgentURI = new URI("system://emailToAgentMap")
  var tokenToEmailURI = new URI("system://tokenToEmailMap")

  def handle(k: AgentCRUDResponse => Unit): Unit = {
    id match {
      case ExternalIdentity(Email(address)) => {
        // Check if email is already registered in the system
        agentMgr().read(
          // TODO(mike): use proper interpolation
          fromTermString("email(\"" + address + "\", EncryptedAgentURI)").get,
          List(Conversion.selfConnection(emailToAgentURI)),
          (optRsrc: Option[mTT.Resource]) => {
            case None => ()
            // Does not exist; send confiramtion email
            case Some(mTT.RBoundHM(Some(mTT.Ground(Bottom)), _)) => {
              confirmEmail(sessionURI, address, k)
            }
            // Exists; error
            case _ => {
              k(AddAgentExternalIdentityError("Email address is already taken."))
            }
          }
        )
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
    val termStr = "token(" + token + ", " + address + ")"
    agentMgr().put(
      // TODO(mike): use proper interpolation
      fromTermString(termStr).get,
      List(Conversion.selfConnection(tokenToEmailURI)),
      termStr,
      (optRsrc: Option[mTT.Resource]) => {
        case None => ()
        case Some(_) => {
          ConfirmationEmail.confirm(address, token)
          k(AddAgentExternalIdentityWaiting(sessionURI))
        }
      }
    )
  }
}

case class AddAgentExternalIdentityToken(sessionURI: URI, token: String) extends AgentCrudRequest {
  def handle(k: AgentCRUDResponse => Unit): Unit = {
    val tokenTermStr = "token(" + token + ", EmailAddress)"
    
    agentMgr().get(
      fromTermString(tokenTermStr).get,
      List(Conversion.selfConnection(tokenToEmailURI)), 
      (optRsrc: Option[mTT.Resource]) => {
        optRsrc match {
          case None => ()
          case Some(mTT.RBoundHM(Some(mTT.Ground( v )), bindings)) => {
            v match {
              case Bottom => {
                k(AddExternalIdentityError("No such token."))
              }
              case PostedExpr(_) => {
                val address = bindings.get("EmailAddress")
                addEmailToIdentities(sessionURI, address, k)
              }
            }
          }
          case _ => throw new Exception("Unrecognized resource: " + rsrc)
        }
      }
    )
  }

  def addEmailToIdentities(sessionURI: URI, address: String, k: AgentCRUDResponse => Unit): Unit = {
    val eiTerm = fromTermString("externalIdentities(Identities)").get
    agentMgr().get(
      eiTerm,
      List(Conversion.sessionToAgent(sessionURI)),
      (optRsrc: Option[mTT.Resource]) => {
        case None => ()
        case Some(mTT.RBoundHM(Some(mTT.Ground( v )), bindings)) => {
          v match {
            case Bottom => {
              // create externalIdentities on Agent
              putIdentities(sessionURI, List(ExternalIdentity(Email(address))), k)
            }
            case PostedExpr(_) => {
              // add email to externalIdentities, then put
              val identities = bindings.get("Identities")
              // convert from prolog to List[ExternalIdentities]
              val idList = *****
              putIdentities(sessionURI, idList :+ ExternalIdentity(Email(address)), k)
            }
          }
        }
        case _ => throw new Exception("Unrecognized resource: " + rsrc)
      }
    )
  }
  
  def putIdentities(sessionURI: URI, ids: List[ExternalIdentity], k: AgentCRUDResponse => Unit): Unit = {
    val idsTermStr = "externalIdentities(" + ids.map(_.asProlog).mkString(",") + ")"
    agentMgr.put(
      fromTermString(idsTermStr).get,
      Conversion.sessionToAgent(sessionURI),
      idsTermStr,
      (optRsrc: Option[mTT.Resource]) => {
        case None => ()
        case Some(_) => {
          k(AddAgentExternalIdentityResponse(sessionURI))
        }
      }
    )
  }
  
  // Compute the mac of an email address
  def emailToCap(email: String): String = {
    val macInstance = Mac.getInstance("HmacSHA256")
    macInstance.init(new SecretKeySpec("emailmac".getBytes("utf-8"), "HmacSHA256"))
    macInstance.doFinal(email.getBytes("utf-8")).map("%02x" format _).mkString.substring(0,36)
  }

  // Given an email, mac it, then create Cnxn(mac, "emailhash", mac) and post "email(X): mac"
  // to show we know about the email.  Return the mac
  def storeCapByEmail(email: String): String = {
    val cap = emailToCap(email)
    val emailURI = new URI("emailhash://" + cap)
    val emailSelfCnxn = //new ConcreteHL.PortableAgentCnxn(emailURI, emailURI.toString, emailURI)
      PortableAgentCnxn(emailURI, "emailhash", emailURI)
    agentMgr().put[String](
      emailLabel,
      List(emailSelfCnxn),
      cap
    )
    cap
  }
}
