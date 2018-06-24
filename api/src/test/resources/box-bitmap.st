; A bitmap that defines a box as given by Applesoft BASIC Programmer's Reference Manual
; The bitmap is transformed into a vector by BitmapShape#toVector.

; Notes:
;   x = plot
;   . = unplotted; used to clarify image regions
;   + = origin, no plot (assumed to be upper-left if unspecified)
;   * = origin. plot
;   whitespace is ignored

.bitmap label-bitmap
    .xxx.
    x...x
    x.+.x
    x...x
    .xxx.
