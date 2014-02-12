List =
  map: (f,arr) ->
    f x for x in arr

  foldLeft: (f,acc,arr) ->
    if arr.length == 0
      acc
    else
      [hd,tl...] = arr
      this.foldLeft f, (f acc, hd), tl

  foldRight: (f,arr,acc) ->
    g = curry (continuation, x, solution) -> f x, (continuation solution)
    (this.foldLeft g, ((id) -> id), arr) acc

  zip: (arrA, arrB) ->
    if arrA.length != arrB.length
      alert('Arrays must be the same length')
    else
      unsafe_zip = (arrA, arrB, acc) ->
        if arrA.length==0
          acc
        else
          [hdA,tlA...] = arrA
          [hdB,tlB...] = arrB
          unsafe_zip tlA, tlB, acc.concat([[hdA,hdB]])
      unsafe_zip(arrA, arrB, [])

  unzip: (arrAB) ->
    foldUnzip = ([accA,accB],[a,b]) -> [[accA...,a],[accB...,b]]
    this.foldLeft foldUnzip, [], arrAB

  map2: (f, arrA, arrB) ->
    this.map (([x,y]) -> f x, y), this.zip arrA, arrB

  foldLeft2: (f, acc, arrA, arrB) ->
    this.foldLeft ((acc, [x,y]) -> f acc, x, y), acc, (this.zip arrA, arrB)

  foldRight2: (f, arrA, arrB, acc) ->
    this.foldRight (([x,y], acc) -> f x, y, acc), (this.zip arrA, arrB), acc