# BASIC Tools API

The BASIC Tools API is a set of reusable code that can be used to parse a text-based Applesoft BASIC program an generate the appropriate tokens.  It also has multiple types of visitors that can re-write that parse tree to rearrange the code (calling them optimizations is a bit over-the-top).

## Maven / Gradle

To include in a Maven project:

```xml
<dependency>
  <groupId>net.sf.applecommander</groupId>
  <artifactId>bastools-api</artifactId>
  <version>0.3.0</version>
</dependency>
```

To include in a Gradle project:

```
dependencies {
    // ...
    compile "net.sf.applecommander:bastools-api:0.3.0"
    // ...
}
```

## API descriptions

Currently the API is broken into the following sections:

* [BASIC Tokenizer](README-TOKENIZER.md)
* [Shape Tooling](README-SHAPES.md)
