
startCometListener = (sessionURI) =>
    reportAndContinue = (r, s, x...) =>
      echo s
      startCometListener(sessionURI)
    api.ping sessionURI
      success: reportAndContinue
      error: reportAndContinue
      complete: reportAndContinue