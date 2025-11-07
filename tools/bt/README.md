## Usage

```shell
Usage: bt [-chOVx] [--addresses] [--applesingle] [--debug] [--list] [--pretty]
          [--stdout] [--tokens] [--variables] [--wrapper] [-a=<address>]
          [--max-line-length=<maxLineLength>] [-o=<outputFile>]
          [-f=<optimizations>[,<optimizations>...]]... [--preserve | --modern |
          --classic] <sourceFile>

Transforms an AppleSoft program from text back to its tokenized state.
      <sourceFile>          AppleSoft BASIC program to process.

Options:
  -a, --address=<address>   Base address for program
                              Default: 2049
      --addresses           Dump line number addresses out.
      --applesingle         Write output in AppleSingle format
  -c, --copy                Generate a copy/paste form of output for testing in
                              an emulator.
      --debug               Print debug output.
  -f=<optimizations>[,<optimizations>...]
                            Enable specific optimizations.
                            * remove-empty-statements - Strip out all '::'-like
                              statements.
                            * remove-rem-statements - Remove all REM statements.
                            * shorten-variable-names - Ensure all variables are
                              1 or 2 characters long.
                            * extract-constant-values - Assign all constant
                              values first.
                            * merge-lines - Merge lines.
                            * renumber - Renumber program.
                            * shorten-numbers - Shorten numbers.
  -h, --help                Show this help message and exit.
      --list                List structure as bastools understands it.
      --max-line-length=<maxLineLength>
                            Maximum line length for generated lines.
                              Default: 255
  -o, --output=<outputFile> Write binary output to file.
  -O, --optimize            Apply all optimizations.
      --pretty              Pretty print structure as bastools understands it.
      --stdout              Send binary output to stdout.
      --tokens              Dump token list to stdout for debugging.
  -V, --version             Print version information and exit.
      --variables           Generate a variable report
      --wrapper             Wrap the Applesoft program (DOS 3.3).
  -x, --hex                 Generate a binary hex dump for debugging.

Tokenizer Selection:
      --classic             Select classic tokenizer
      --modern              Select modern tokenizer (default)
      --preserve            Select classic tokenizer with number preservation

Tokenizer Defaults:
  Option      Tokenizer Class     Parsing?   Numbers?   DATA?
  ----------  ------------------  ---------  ---------  ---------
  --modern    ModernTokenReader   'Modern'   Rewritten  Rewritten
  --classic   ClassicTokenReader  Applesoft  Rewritten  Preserved
  --preserve  ClassicTokenReader  Applesoft  Preserved  Preserved
  ----------  ------------------  ---------  ---------  ---------
  * Parsing: 'Modern' -  spaces between keywords and tokens are important,
                         any variable name can be used;
             Applesoft - ignores spaces, special logic to disambiguate AT/ATN/A TO,
                         variables cannot have keywords in them (ex: TON is invalid).
  * Numbers: Rewritten - means that a 0.600 is output as 0.6;
             Preserved - means that a 0.600 is output as 0.600.
  * Data:    Rewritten - the tokenizer identifies the data type and handles it appropriately;
             Preserved - the statement text (including all whitespace) is preserved.
```

## Using copy and paste

If your Apple emulator supports copy and paste (not all do!), this is handy when experimenting.

```shell
$ bt --copy tools/bt/src/test/resources/circles.bas 
0067:01 08 
00AF:E2 09 
0800:00 
0801:0A 08 0A 00 AB 31 30 30 00 23 08 14 00 B2 64 72 
0811:61 77 20 63 69 72 63 6C 65 20 72 6F 75 74 69 6E 
0821:65 00 2F 08 1E 00 81 41 D0 30 C1 50 54 00 47 08 
0831:28 00 58 D0 58 28 41 29 CA 53 5A 3A 59 D0 59 28 
0841:41 29 CA 53 5A 00 56 08 32 00 93 58 4F C8 58 2C 
0851:59 4F C8 59 00 65 08 3C 00 93 58 4F C9 58 2C 59 
0861:4F C8 59 00 74 08 46 00 93 58 4F C8 58 2C 59 4F 
0871:C9 59 00 83 08 50 00 93 58 4F C9 58 2C 59 4F C9 
0881:59 00 8A 08 5A 00 82 41 00 90 08 5F 00 B1 00 A2 
0891:08 64 00 B2 6D 61 69 6E 20 70 72 6F 67 72 61 6D 
08A1:00 A8 08 6E 00 91 00 D6 08 73 00 43 28 30 29 D0 
08B1:31 3A 43 28 31 29 D0 32 3A 43 28 32 29 D0 33 3A 
08C1:43 28 33 29 D0 35 3A 43 28 34 29 D0 36 3A 43 28 
08D1:35 29 D0 37 00 F5 08 78 00 97 3A A2 32 31 3A 9E 
08E1:3A BA 22 4A 55 53 54 20 41 20 4D 4F 4D 45 4E 54 
08F1:22 3A 9D 00 04 09 82 00 50 49 D0 33 2E 31 34 31 
0901:35 39 00 1B 09 8C 00 50 54 D0 33 30 3A 86 58 28 
0911:50 54 29 2C 59 28 50 54 29 00 27 09 96 00 81 41 
0921:D0 30 C1 50 54 00 3B 09 A0 00 42 D0 50 49 CA 28 
0931:41 CB 28 50 54 CA 32 29 29 00 49 09 AA 00 58 28 
0941:41 29 D0 DF 28 42 29 00 57 09 B4 00 59 28 41 29 
0951:D0 DE 28 42 29 00 5E 09 BE 00 82 41 00 68 09 C8 
0961:00 97 3A A2 32 31 00 75 09 D2 00 81 51 D0 31 C1 
0971:31 30 30 00 88 09 D7 00 43 D0 36 CA DB 28 31 29 
0981:3A 92 43 28 43 29 00 9C 09 DC 00 53 5A D0 31 30 
0991:C8 28 34 30 CA DB 28 31 29 29 00 B6 09 E6 00 58 
09A1:4F D0 28 32 37 39 C9 53 5A CA 32 29 CA DB 28 31 
09B1:29 C8 53 5A 00 D0 09 F0 00 59 4F D0 28 31 35 39 
09C1:C9 53 5A CA 32 29 CA DB 28 31 29 C8 53 5A 00 D8 
09D1:09 FA 00 B0 33 30 00 DF 09 04 01 82 51 00 00 00 
```

## Listing / Optimization

Listing is handy when tinkering with what the tool does.  Before optimization...

```shell
$ bt --list tools/bt/src/test/resources/circles.bas 
10  GOTO 100
20 REM draw circle routine
30  FOR A = 0 TO PT
40 X = X(A) * SZ:Y = Y(A) * SZ
50  HPLOT XO + X,YO + Y
60  HPLOT XO - X,YO + Y
70  HPLOT XO + X,YO - Y
80  HPLOT XO - X,YO - Y
90  NEXT A
95  RETURN 
100 REM main program
110  HGR 
115 C(0) = 1:C(1) = 2:C(2) = 3:C(3) = 5:C(4) = 6:C(5) = 7
120  HOME : VTAB 21: INVERSE : PRINT "JUST A MOMENT": NORMAL 
130 PI = 3.14159
140 PT = 30: DIM X(PT),Y(PT)
150  FOR A = 0 TO PT
160 B = PI * (A / (PT * 2))
170 X(A) =  SIN (B)
180 Y(A) =  COS (B)
190  NEXT A
200  HOME : VTAB 21
210  FOR Q = 1 TO 100
215 C = 6 *  RND (1): HCOLOR= C(C)
220 SZ = 10 + (40 *  RND (1))
230 XO = (279 - SZ * 2) *  RND (1) + SZ
240 YO = (159 - SZ * 2) *  RND (1) + SZ
250  GOSUB 30
260  NEXT Q
```

... and after optimization ...

```shell
$ bt --optimize --list tools/bt/src/test/resources/circles.bas 
0 D=0:E=1:F=2:G=3:H=5:I=4:J=6:K=7:L=21:M=100:N=10:O=40:P=279:R=159: GOTO 2
1  FOR A = D TO PT:X = X(A) * SZ:Y = Y(A) * SZ: HPLOT XO + X,YO + Y: HPLOT XO - X,YO + Y: HPLOT XO + X,YO - Y: HPLOT XO - X,YO - Y: NEXT A: RETURN 
2  HGR :C(D) = E:C(E) = F:C(F) = G:C(G) = H:C(I) = J:C(H) = K: HOME : VTAB L: INVERSE : PRINT "JUST A MOMENT": NORMAL :PI = 3.14159:PT = 30: DIM X(PT),Y(PT): FOR A = D TO PT:B = PI * (A / (PT * F)):X(A) =  SIN (B):Y(A) =  COS (B): NEXT A: HOME : VTAB L: FOR Q = E TO M:C = J *  RND (E): HCOLOR= C(C):SZ = N + (O *  RND (E)):XO = (P - SZ * F) *  RND (E) + SZ:YO = (R - SZ * F) *  RND (E) + SZ: GOSUB 1: NEXT Q
```

Specific optimizations may also be triggered:

```shell
$ bt -fremove-rem-statements,merge-lines --list tools/bt/src/test/resources/circles.bas 
10  GOTO 110
30  FOR A = 0 TO PT:X = X(A) * SZ:Y = Y(A) * SZ: HPLOT XO + X,YO + Y: HPLOT XO - X,YO + Y: HPLOT XO + X,YO - Y: HPLOT XO - X,YO - Y: NEXT A: RETURN 
110  HGR :C(0) = 1:C(1) = 2:C(2) = 3:C(3) = 5:C(4) = 6:C(5) = 7: HOME : VTAB 21: INVERSE : PRINT "JUST A MOMENT": NORMAL :PI = 3.14159:PT = 30: DIM X(PT),Y(PT): FOR A = 0 TO PT:B = PI * (A / (PT * 2)):X(A) =  SIN (B):Y(A) =  COS (B): NEXT A: HOME : VTAB 21: FOR Q = 1 TO 100:C = 6 *  RND (1): HCOLOR= C(C):SZ = 10 + (40 *  RND (1)):XO = (279 - SZ * 2) *  RND (1) + SZ:YO = (159 - SZ * 2) *  RND (1) + SZ: GOSUB 30: NEXT Q
```

## Piping to stdout

`bt` can also pipe an AppleSingle file to stdout, which can then be read in for AppleCommander 1.5 or later.  Note that file type, address, and name is set automatically.

```shell
$ ac -pro140 demo.dsk demo
$ bt --optimize --applesingle --stdout tools/bt/src/test/resources/circles.bas | ac -as demo.dsk
$ ac -l demo.dsk 
demo.dsk /DEMO/
  CIRCLES BAS 001 05/29/2018 05/29/2018 338 A=$0801 
ProDOS format; 139,264 bytes free; 4,096 bytes used.

```

## Wrapping the application

DOS 3.3 (but not ProDOS) seems to rewrite the application linked list when an Applesoft program is loaded; this rewrites the pointers and impacts any embedded (via `$embed`) machine code. With the wrapper, the application is "wrapped" with a startup Applesoft program that prevents the rewrite. The wrapper is just a simple program:

```basic
10 POKE 103,24:POKE 104,8:RUN
```

This is a valid program that resets the Applesoft pointer to just after the current program and runs that other program.

## Preserving input

`bt` now supports a "classic" tokenizer that has the ability to preserve numbers in the source code (instead of simplifying them). 
The primary motivation is for those typing in magazine programs that have check programs... altering the code defeats the usefulness
of the check algorithm.

For instance:

```shell
# Note the extended digits...
$ cat ticket-49a.bas 
10 PRINT "MATHING"
30 A = .4
40 B = 0.6000
50 C = -.250
60 D = -0.70
70 PRINT "A=";A
80 PRINT "B=";B
90 PRINT "C=";C
95 PRINT "D=";D
# ... Those digits remain in the listing...
$ bt --preserve --list ticket-49a.bas
10  PRINT "MATHING"
30 A = .4
40 B = 0.6000
50 C =  - .250
60 D =  - 0.70
70  PRINT "A=";A
80  PRINT "B=";B
90  PRINT "C=";C
95  PRINT "D=";D
# ... A bit harder to see, but the digits remain in the program bytes as well
$ bt --preserve --hex ticket-49a.bas
0801: 10 08 0a 00 ba 22 4d 41 54 48 49 4e 47 22 00 19  ....."MATHING"..
0811: 08 1e 00 41 d0 2e 34 00 26 08 28 00 42 d0 30 2e  ...A..4.&.(.B.0.
0821: 36 30 30 30 00 32 08 32 00 43 d0 c9 2e 32 35 30  6000.2.2.C...250
0831: 00 3e 08 3c 00 44 d0 c9 30 2e 37 30 00 4a 08 46  .>.<.D..0.70.J.F
0841: 00 ba 22 41 3d 22 3b 41 00 56 08 50 00 ba 22 42  .."A=";A.V.P.."B
0851: 3d 22 3b 42 00 62 08 5a 00 ba 22 43 3d 22 3b 43  =";B.b.Z.."C=";C
0861: 00 6e 08 5f 00 ba 22 44 3d 22 3b 44 00 00 00 ..  .n._.."D=";D... 
```
