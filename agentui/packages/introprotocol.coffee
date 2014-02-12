

api.createUser = (email, password, profile) => (settings) =>
  api.post _.extend settings,
    "msgType": "createUserRequest"
    "content":
      "email": email
      "password": password
      "jsonBlob": profile

api.initializeSession = (agentURI) => (settings) =>
  api.post _.extend settings,
    "msgType": "initializeSessionRequest"
    "content":
      "agentURI": agentURI

api.beginIntroduction = (sessionURI, alias, cnxnA, msgA, cnxnB, msgB) => (settings) =>
  api.post _.extend settings,
    "msgType": "beginIntroductionRequest"
    "content":
      "sessionURI": sessionURI
      "alias": alias
      "aConnection": cnxnA
      "aMessage": msgA
      "bConnection": cnxnB
      "bMessage": msgB

api.confirmIntroduction = (sessionURI, alias, introSessionID, correlationID, accepted) =>
  api.post _.extend settings,
    "msgType": "introductionConfirmationRequest"
    "content":
      "sessionURI": sessionURI
      "alias": alias
      "introSessionID": introSessionID
      "correlationID": correlationID
      "accepted": accepted