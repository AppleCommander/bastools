# BT API

The BASIC Tokenizer API is a set of reusable code that can be used to parse a text-based AppleSoft BASIC program an generate the appropriate tokens.  It also has multiple types of visitors that can re-write that parse tree to rearrange the code (calling them optimizations is a bit over-the-top).

## Maven / Gradle

To include in a Maven project:

```xml
<dependency>
  <groupId>net.sf.applecommander</groupId>
  <artifactId>bastokenizer-api</artifactId>
  <version>0.2.0</version>
</dependency>
```

To include in a Gradle project:

```
dependencies {
    // ...
    compile "net.sf.applecommander:bastokenizer-api:0.2.0"
    // ...
}
```

## Overview

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

`$embed` will allow a binary to be embedded within the resulting application *and will move it to a destination in memory*. Please note that once the application is loaded on the Apple II, the program cannot be altered as the computer will crash.  Usage example:

```
5 $embed "read.time.bin", "0x0260"
```

The `$embed` directive _must_ be last on the line (if there are comments, be sure to use the `REMOVE_REM_STATEMENTS` optimization. It takes two parameters: file name and target address, both are strings.

From the `circles-timing.bas` sample, this is the beginning of the program:

```
0801:9A 09 00 00 8C 32 30 36 32 3A AB 31 00 A9 2B 85
     \___/ \___/ \____________/    \___/    \_______...
     Ptr, Line 0, CALL 3062,    :, GOTO 1,   Assembly code...     
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

### `$hex`

If embedding hexidecimal addresses into an application makes sense, the `$hex` directive allows that to be done in a rudimentary manner.

Sample:

```
10 call $hex "fc58"
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
