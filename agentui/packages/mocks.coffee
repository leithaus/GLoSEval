

defaultHandlers =
  success: (jqXHR, textStatus) =>
    echo "Success!"
    echo textStatus
  error: (jqXHR, status, error) =>
    echo "Error!"
    echo status
    echo error
  complete: (jqXHR, textStatus) =>