IokeGround Sequence = Origin mimic
Sequence mimic!(Mixins Enumerable)

Sequence each = dmacro(
  [chain]
  while(next?,
    chain evaluateOn(call ground, next)
  )
  @,

  [argumentName, code]
  lexicalCode = LexicalBlock createFrom(list(argumentName, code), call ground)
  while(next?,
    lexicalCode call(next)
  )
  @,

  [indexArgumentName, argumentName, code]
  lexicalCode = LexicalBlock createFrom(list(indexArgumentName, argumentName, code), call ground)
  index = 0
  while(next?,
    lexicalCode call(index, next)
    index++
  )
  @
)

Mixins Sequenced do(
  mapped = macro(call resendToReceiver(self seq))
  collected = macro(call resendToReceiver(self seq))
  filtered = macro(call resendToReceiver(self seq))
  selected = macro(call resendToReceiver(self seq))
  grepped = macro(call resendToReceiver(self seq))
  zipped = macro(call resendToReceiver(self seq))
  dropped = macro(call resendToReceiver(self seq))
  droppedWhile = macro(call resendToReceiver(self seq))
  rejected = macro(call resendToReceiver(self seq))
)



; Sequence Filter do(
;   next = method(
;     if(@current?,
;       @current? = false
;       @current,
;       while(@wrappedSequence next?,
;         n = @wrappedSequence next
;         x = transformValue(cell(:n))
;         if(cell(:x),
;           return(cell(:n)))
;       )
;     )
;   )

;   next? = method(
;     if(@current?,
;       @current? = false
;       true,
;       while(@wrappedSequence next?,
;         n = @wrappedSequence next
;         x = transformValue(cell(:n))
;         if(cell(:x),
;           @current? = true
;           @current = cell(:n)
;           return(true)
;         )
;       )
;       false)
;   )
; )

; Sequence Map do(
;   next = method(
;     if(@current?,
;       @current? = false
;       @current,
;       while(@wrappedSequence next?,
;         n = @wrappedSequence next
;         x = transformValue(cell(:n))
;         if(TAKE_CURRENT_VALUE?,
;           return(RETURN_OBJECT))
;       )
;     )
;   )

;   next? = method(
;     if(@current?,
;       @current? = false
;       true,
;       while(@wrappedSequence next?,
;         n = @wrappedSequence next
;         x = transformValue(cell(:n))
;         if(TAKE_CURRENT_VALUE?,
;           @current? = true
;           @current = RETURN_OBJECT
;           return(true)
;         )
;       )
;       false)
;   )
; )





; Sequence Map do(
;   next = method(
;     n = @wrappedSequence next
;     x = transformValue(cell(:n))
;     cell(:x)
;   )

;   next? = method(
;     @wrappedSequence next?
;   )
; )


let(
  generateNextPMethod, method(takeCurrentObject, returnObject,
    ''method(
      if(@current?,
        true,
        while(@wrappedSequence next?,
          n = @wrappedSequence next
          x = transformValue(cell(:n))
          if(`takeCurrentObject,
            @current? = true
            @current = `returnObject
            return(true)
          )
        )
        false)
      ) evaluateOn(@)
    ),

  generateNextMethod, method(takeCurrentObject, returnObject,
    ''method(
      if(@current?,
        @current? = false
        @current,
        while(@wrappedSequence next?,
          n = @wrappedSequence next
          x = transformValue(cell(:n))
          if(`takeCurrentObject,
            return(`returnObject)))
      )
    ) evaluateOn(@)
    ),

  sequenceObject, dmacro(
    [takeCurrentObject, returnObject]
    s = Sequence Base mimic
    s next? = generateNextPMethod(takeCurrentObject, returnObject)
    s next  = generateNextMethod(takeCurrentObject, returnObject)
    s
    ),

  Sequence Base   = Sequence mimic do(current? = false)
  Sequence Base create = method(wrappedSequence, context, messages, +rest,
    res = mimic
    res wrappedSequence = wrappedSequence
    res context = context
    res messages = messages
    res restArguments = rest
    if(messages length == 2,
      res lexicalBlock = LexicalBlock createFrom(messages, context)
    )
    res
  )

  Sequence Base transformValue = method(inputValue,
    if(messages length == 0,
      cell(:inputValue),
      if(messages length == 1,
        messages[0] evaluateOn(context, cell(:inputValue)),
        lexicalBlock call(cell(:inputValue)))
    )
  )

  Sequence Map       = sequenceObject(true,     cell(:x))
  Sequence Filter    = sequenceObject(cell(:x), cell(:n))

  Sequence Reject    = sequenceObject(!cell(:x), cell(:n))
  Sequence Grep      = sequenceObject(restArguments[0] === cell(:n), cell(:x))
  Sequence Drop      = sequenceObject(if(restArguments[0] == 0, true, restArguments[0] = restArguments[0] - 1. false), cell(:n))
  Sequence DropWhile = sequenceObject(
    unless(@collecting,
      unless(cell(:x),
        @collecting = true,
        false),
      true),
    cell(:n)) do(collecting = false)

  Sequence Zip       = sequenceObject(true,
    resultList = list(cell(:n))
    restArguments each(rr,
      resultList << if(rr next?, rr next, nil))
    resultList
  ) do(
    baseCreate = Sequence Base cell(:create)
    create = method(+args,
      myNewSelf = baseCreate(*args)
      myNewSelf restArguments map!(x,
        if(x mimics?(Sequence),
          x,
          x seq)
      )
      myNewSelf
    )
  )
)
