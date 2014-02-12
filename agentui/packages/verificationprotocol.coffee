
api ?= {}

api.tapClaimant = (token, claim, v2c, v2r) => (settings) =>
  api.post _.extend settings,
    msgType: "tapClaimant"
    content:
      token: token
      claim: claim
      claimant: v2c
      relyingParty: v2r

api.validateClaim = (token, claim, res, v2c, v2r) => (settings) =>
  api.post _.extend settings,
    msgType: "validateClaim"
    content:
      token: token
      claim: claim
      claimant: v2c
      relyingParty: v2r

api.produceClaim = (token, claim, r2c, r2v) => (settings) =>
  api.post _.extend settings,
    msgType: "produceClaim"
    content:
      token: token
      claim: claim
      claimant: r2c
      verifier: r2v

api.completeClaim = (token, claim, r2c, r2v) => (settings) =>
  api.post _.extend settings,
    msgType: "completeClaim"
    content:
      token: token
      claim: claim
      claimant: r2c
      verifier: r2v

api.authorizeVerifier = (token, claim, c2v, c2r) => (settings) =>
  api.post _.extend settings,
    msgType: "authorizeVerifier"
    content:
      token: token
      claim: claim
      verifier: c2v
      relyingParty: c2r

api.submitClaim = (token, claim, c2v, c2r) => (settings) =>
  api.post _.extend settings,
    msgType: "submitClaim"
    content:
      token: token
      claim: claim
      verifier: c2v
      relyingParty: c2r