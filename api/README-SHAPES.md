# Shape Tooling

The Shape API allows:
* Shape tables to be read in the standard binary format;
* Shape tables to be generated from "source" in three formats;
* Shape tables to be written to the standard binary format;
* Shapes and shape tables can be written to a text or image graphical representation. 

## API Notes

The shape table is represented by the `ShapeTable` class which has static `read` methods.
To generate a shape table from "source" use the `ShapeGenerator` class.

The `ShapeTable` object holds a list of `Shape`s.  A `Shape` can be converted to a `VectorShape`
(up, down, left, right, plot/no plot) or to a `BitmapShape` with the `Shape#toVector()` and
`Shape#toBitmap()` methods.

## Shape source

These samples define the same shape as given by Applesoft BASIC Programmer's Reference Manual - a box.

## Shape source - bitmap format

To introduce a bitmap shape, use the `.bitmap` directive.

The bitmap defines an XY grid of plot/no-plot zones.  An origin may be specified and if not specified defaults to (0,0).

Notes:

* `x` = plot
* `.` = no plot; used to clarify image regions
* `+` = origin, no plot (assumed to be upper-left if unspecified)
* `*` = origin. plot
* whitespace is ignored

Sample:

```
.bitmap
    .xxx.
    x...x
    x.+.x
    x...x
    .xxx.
```

## Shape source - long vector format

To introduce a long vector shape, use the `.long` directive.

Notes:

* `move`[`up`|`down`|`left`|`right`] = move vector
* `plot`[`up`|`down`|`left`|`right`] = plot vector
* whitespace is ignored
* case insensitive
* accepts a numerical argument for repetition

```
.long
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
```

## Shape source - short vector format

To introduce a short vector shape, use the `.short` directive.

Notes:

* `u`, `d`, `l`, `r` = move vector
* `U`, `D`, `L`, `R` = plot vector
* whitespace is ignored
* case sensitive

```
.short
    dd
    LL
    uUUU
    rRRR
    dDDD
    lL
```
