# BT API

The BASIC Tokenizer API is a set of reusable code that can be used to parse a text-based AppleSoft BASIC program an generate the appropriate tokens.  It also has multiple types of visitors that can re-write that parse tree to rearrange the code (calling them optimizations is a bit over-the-top).

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

## Optimizations

## Visitors

