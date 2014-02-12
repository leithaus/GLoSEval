# Functional Programming Essentials
curry = (f) ->
  partial = (args...) ->
    if args.length > f.length
      alert 'too many arguments'
    else if args.length == f.length
      f args...
    else
      (more...) ->
          partial (args.concat(more))...

map = (f,arr) ->
  f x for x in arr

fold_left = (f,acc,arr) ->
  if arr.length == 0
    acc
  else
    [hd,tl...] = arr
    fold_left f, (f acc, hd), tl

fold_right = (f,arr,acc) ->
  g = curry (continuation, x, solution) -> f x, (continuation solution)
  (fold_left g, ((id) -> id), arr) acc

memoize = (fun) ->
  (args...) ->
    arguments.callee[args] ?= fun args...

# Echo

echo = (obj) ->
  toString = (obj) ->
    if obj instanceof Array
      if obj.length == 0
        "[]"
      else
        "[" + (map toShortString, obj).join(",") + "]"
    else if obj instanceof Function
      obj.toString()
    else if obj instanceof Object
      if obj.length == 0
        "[]"
      else
        mapFields = (key) -> key + " = " + toShortString obj[key]
        fields = (map mapFields, (Object.keys obj))
        "{ " + (fields.join ",\n  ") + " }"
    else
      obj.toString()
  toShortString = (obj) ->
    if obj instanceof Array
      if obj.length == 0
        "[]"
      else
        "[" + (map toShortString, obj).join(",") + "]"
    else if obj instanceof Function
      $.trim (obj.toString().replace /([^{]*)(.*\n)*.*/, '$1')
    else if obj instanceof Object
      if (Object.keys obj).length ==0
        "{}"
      else
        "{..}"
    else
      obj.toString()
  try
    text = toString obj
  catch error
    text = 'undef'
  el = $('#console')[0]
  $(el).text ($(el).text() + text + '\n')

#Pre-Processing
pre_process = (doc) ->
  doc.replace /#include (.+)/g, "eval ((include '$1').js)"

# Meta-Management

prompt = ''
command_sequence = ''
static_error = ''
transient = ''

updateMeta = ->
  meta = $('#meta')
  if transient != ''
    if transient != meta.text()
      meta.text transient
      meta.css('color', 'white')
      meta.show()
    else
      transient = ''
      updateMeta()
  else if prompt != ''
    meta.text prompt
    meta.css('color', 'white')
    meta.show()
  else if command_sequence != ''
    meta.text command_sequence
    meta.css('color', 'white')
    meta.show()
  else if static_error != ''
    meta.text static_error
    meta.css('color', '#862322')
    meta.show()
  else
    meta.hide()

interpretCommand = ->
  if command_sequence.match /\^G-/
    transient = 'Quit'; return true
  return false


# Manage includes with memoization
include = memoize (package_name) ->
  package = undefined
  $.ajax
    url: ('packages/'+package_name+'.coffee')
    success:
      (data, textStatus, jqXHR) ->
        package =
          source: data
          js: (CoffeeScript.compile (pre_process data), bare: on)
    error:
      (jqXHR, textStatus, errorThrown) ->
        echo 'Error loading '  + package_name + ': ' + errorThrown
    async: false
  package

# Set up the compilation function to run when you stop typing.
compileSource = ->
  source = pre_process ($('#editor').val())
  window.compiledJS = ''
  try
#    window.compiledJS = CoffeeScript.compile prelude, bare: on
    $('#output').text CoffeeScript.compile source, bare: on
    static_error = ''
  catch error
    static_error = error.message
  updateMeta()

# Listen for keypresses and recompile.
$('#editor').keyup -> compileSource()

# Eval the compiled js.
evalJS = ->
  $('#console').text ''
  try
    eval $('#output').text()
  catch error then echo error.stack

# Helper to hide the menus.
closeMenus = ->
  $('.navigation.active').removeClass 'active'

# Ctrl-Commands
cycleRightPanels = ->
  topLayer = $('.layer:first')
  topPanel = $('.layer:first>.panel.right:first')
  topLayer.append topPanel.detach()


# Add hooks.
$(document.body)
  .keydown (e) ->
    (return false) if e.which == 17
    if e.ctrlKey
      switch (String.fromCharCode(e.keyCode))
        when '' then false
        when 'J' then cycleRightPanels()
        else
          (evalJS(); return false) if e.which == 13
          command_sequence += '^' + String.fromCharCode(e.keyCode).toUpperCase() + '-'
          (command_sequence = '') if interpretCommand()
          updateMeta()
      false

compileSource()