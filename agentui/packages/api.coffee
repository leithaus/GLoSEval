api =
  post: (settings) =>
    $.ajax
      type: "POST"
      url: "/api"
      data: JSON.stringify
        "msgType": settings?.msgType, 
        "content": settings?.content,
      success: (jqXHR, textStatus) => 
        settings?.success?(jqXHR, textStatus)
      error: (jqXHR, status, error) =>
        settings?.error?(jqXHR, status, error)
      complete: (jqXHR, textStatus) => 
        settings?.complete?(jqXHR, textStatus)

api.ping = (sessionURI) => (settings) =>
  api.post _.extend settings,
   "sessionURI": sessionURI

arr = (source, label, target) =>
  "source": source
  "label": label
  "target": target