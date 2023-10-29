# Tokenizer Overview

Generally, the usage pattern is:
1. Setup the `Configuration`.
2. Read the tokens.
3. Parse the tokens into a `Program`.
4. Apply transformations, if applicable.

## Code snippets

```java
Configuration config = Configuration.builder()
        .sourceFile(this.sourceFile)
        .build();
```

The `Configuration` class also allows the BASIC start address to be set (defaults to `0x801`), set the maximum line length (this is in bytes, and defaults to `255`, but feel free to experiment).  Some of the classes report output via the debug stream, which defaults to a simple null stream (no output) - replace with `System.out` or another `PrintStream`.

```java
Queue<Token> tokens = TokenReader.tokenize(config.sourceFile);
```

The list of tokens is a loose interpretation. It includes more of a compiler sense of tokens -- numbers, end of line markers (they're significant), AppleSoft tokens, strings, comments, identifiers, etc.

```java
Parser parser = new Parser(tokens);
Program program = parser.parse();
```

The `Program` is now the parsed version of the BASIC program.  Various `Visitor`s may be used to report, gather information, or manipulate the tree in various ways.

## Directives

The framework allows embedding of directives.

### `$embed`

> NOTE: It appears that DOS 3.3 _rewrites_ the resulting application and messes up the linked list of lines. ProDOS does not.

`$embed` will allow a binary to be embedded within the resulting application and can move it to a destination in memory. Please note that once the application is loaded on the Apple II, the program cannot be altered as the computer will crash.  

Options:
* `file=<string>`, required. Specifies the file to load.
* `moveto=<addr>`, optional. If provided, generates code to move binary to destination. Automatically `CALL`ed.
* `var=<variable>`, optional. If provided, address is assigned to variable specified.

> Note that the current parser does not handle hex formats (_at all_). You may provide a string as well that starts with a `$` or `0x` prefix.

Usage example:

```
5 $embed file="read.time.bin", moveto="0x0260"
```

The `$embed` directive _must_ be last on the line (if there are comments, be sure to use the `REMOVE_REM_STATEMENTS` optimization.

From the `circles-timing.bas` sample, this is the beginning of the program:

```
0801:9A 09 00 00 8C 32 30 36 32 3A AB 31 00 A9 2B 85
     \___/ \___/ \____________/    \___/    \_______...
     Ptr, Line 0, CALL 2062,    :, GOTO 1,   Assembly code...     
``` 

The move code is based on what Beagle Bros put into their [Peeks, Pokes, and Pointers](https://beagle.applearchives.com/Posters/Poster%202.pdf) poster.  (See _Memory Move_ under the *Useful Calls*; the `CALL -468` entry.)

```
LDA #<embeddedStart
STA $3C
LDA #>embeddedStart
STA $3D
LDA #<embeddedEnd
STA $3E
LDA #>embeddedEnd
STA $3F
LDA #<targetAddress
STA $42
LDA #>targetAddress
STA $43
LDY #0
JMP $FE2C
```

### `$shape`

`$shape` will generate a shape table based either on the source (`src=`) or binary (`bin=`) shape table provided. Source shape table generation is based on the shape table `st` tool support and is described [here in more detail](README-SHAPES.md).

Overall format is as follows:

```
$shape ( src="path" [ ,label=variable | ,assign=(varname1="label1" [,varname2="label2"]* ] ) 
       | bin="path" )
       [,poke=yes(default)|no]
       [,address=<variable>] 
       [,init=yes|no ]
```

#### Shape from source

By using the `src=` option, the source code will be generated on the fly.  For example the following shape source will insert a shape named "mouse" into the BASIC program:

```
; extracted from NEW MOUSE

.bitmap mouse
    ..........*X..  
    ....XXXX.XX...  
    ...XXXXXXXX...  
    .XXXXXXXXXXX..  
    XX.XXXXXXX.XX.  
    X...XXXXXXXXXX  
    XX............  
    .XXX.XX.......  
    ...XXX........  
```

Options on the source include:
* `label=variable` which indicates a label is really a variable name; in the example, the variable name would be "MOUSE".
* `assign=(...)` will define a mapping from the label in the source to the BASIC variable name.  A `assign(m=mouse)` will define the variable `M` to be the shape number for the mouse.

#### Shape from binary

By using the `bin=` option, an already existing binary shape table can be inserted into the code.  There are no additional options available in this case.

#### General options

* `poke=yes|no` (default=`yes`) will embed a `POKE 232,<lowAddr>:POKE 233,<highAddr>` into the line of code.
* `address=<variable>`, if supplied, will assign the address to a variable; therefore a `address=AD` will embed the variable `AD` into the line of code.
* `init=yes|no` (default=`yes`) will embed a simple `ROT=0:SCALE=1` into the line of code for simple shape initialization.

### `$hex`

If embedding hexadecimal addresses into an application makes sense, the `$hex` directive allows that to be done in a rudimentary manner.

Sample:

```
10 call $hex value="fc58"
```

Yields:

```
10 call -936
```

## Optimizations

Optimizations are mechanisms to rewrite the `Program`, typically making the program smaller. `Optimization` itself is an enum which has a `create` method to setup the `Visitor`.

Current optimizations are:
* _Remove empty statements_ will remove all extra colons.  For example, if the application in question used `:` to indicate nesting. Or just accidents!
* _Remove REM statements_ will remove all comments.
* _Extract constant values_ will find all constant numerical references, insert a line `0` with assignments, and finally replace all the numbers with the approrpiate variable name. Hypothesis is that the BASIC interpreter only parses the number once.
* _Merge lines_ will identify all lines that are not a target of `GOTO`/`GOSUB`-type action and rewrite the line by merging it with others.  The concept involved is that the BASIC program is just a linked list and shortening the list will shorten the search path.  The default *max length* in bytes is set to `255`. 
* _Renumber_ will renumber the application, beginning with line `0`. This makes the decoding a tiny bit more efficient in that the number to decode will be smaller in the token stream.

Sample use:

```java
program = program.accept(Optimization.REMOVE_REM_STATEMENTS.create(config));
```
