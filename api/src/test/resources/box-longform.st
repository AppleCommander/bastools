; A vector box as given by Applesoft BASIC Programmer's Reference Manual

; Notes:
;   move[up|down|left|right] = move vector
;   plot[up|down|left|right] = plot vector
;   whitespace is ignored
;   case insensitive

.long label-long
    movedown 2
    plotleft 2
    moveup
    plotup 3
    moveright
    plotright 3
    movedown
    plotdown 3
    moveleft
    plotleft
    