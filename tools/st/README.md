## Overview

`st` is a command-line tool to investigate and (ultimately) author shape tables for inclusion in Applesoft programs.

Samples are extracted from the [original Mouse Maze](https://github.com/a2geek/mouse-maze-2001/tree/master/doc/original), written in 1983.

## Usage

```shell
$ st --help
Usage: st [-hV] [--debug] [COMMAND]

Shape Tools utility

Options:
      --debug     Dump full stack traces if an error occurs
  -h, --help      Show this help message and exit.
  -V, --version   Print version information and exit.

Commands:
  extract   Extract shapes from shape table
  generate  Generate a shape table from source code
  help      Displays help information about the specified command
```

## Sub-command help

```shell
$ st extract --help
Usage: st extract [-h] [--skip-empty] [--stdin] [--stdout]
                  [--border=<borderStyle>] [--format=<outputFormat>]
                  [--shape=<shapeNum>] [-o=<outputFile>] [-w=<width>]
                  [<inputFile>]

Extract shapes from shape table

Parameters:
      [<inputFile>]        File to process

Options:
      --border=<borderStyle>
                           Set border style (none, simple, box)
                             Default: simple
      --format=<outputFormat>
                           Select output format (text, png, gif, jpeg, bmp, wbmp)
                             Default: text
      --shape=<shapeNum>   Extract specific shape
      --skip-empty         Skip empty shapes
      --stdin              Read from stdin
      --stdout             Write to stdout
  -h, --help               Show help for subcommand
  -o, --output=<outputFile>
                           Write output to file
  -w, --width=<width>      Set width (defaults: text=80, image=1024)
```

## Text extract

```shell
$ st --debug extract --stdout --border=box --skip-empty --format=text --width=132 ~/Downloads/shapes/NEW\ MOUSE
┌─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┬─────────────────┐
│XXXXXXXXXXXXXXXX*│...........*X..  │......XXXXX...+  │XXXXXXXXXXXXXXX* │XXXXXXXXXXXXXXX* │.X.X.X.X.X.X.X.* │.X.X.X.X.X.X.X.* │
│X...............X│.....XXXX.XX...  │..XXXXX...XX...  │XXXXXXXXXXXXXXXX │XXXXXXXXXXXXXXXX │.X.X.X.X.X.X.X.X │.X.X.X.X.X.X.X.X │
│X...............X│....XXXXXXXX...  │......XXXXX....  │XXXXXXXXXXXXXXXX │XXXXXXXXXXXXXXXX │.X.X.XXXXXXX.X.X │.X.X.X.....X.X.X │
│X...............X│..XXXXXXXXXXX..  │...............  │XXXXXXXXXXXXXXXX │XXXXXXXXXXXXXXXX │.X..XX.....XX..X │.X.X.X.XXX.X.X.X │
│X...............X│.XX.XXXXXXX.XX.  │....XX..XXX....  │XXXXXXXXXXXXXXXX │XXXXXXXXXXXXXXXX │.X..X.XX.XX.X..X │.X.X..XX.XX..X.X │
│X...............X│.X...XXXXXXXXXX  │.....XX.XXX....  │XXXXXXXXXXXXXXXX │XXXXXXXXXXXXXXXX │.X..XX.....XX..X │.X.X.XX.XX.X.X.X │
│X...............X│.XX............  │.....XX.XXXX...  │XXXXXXXXXXXXXXXX │XXXXXX....XXXXXX │.X.X.XX.X.XX.X.X │.X.X.XXXX..X.X.X │
│X...............X│..XXX.XX.......  │....XX.XXXXX...  │XXXXXXXXXXXXXXXX │XXXXX......XXXXX │.X.X..XX.XX..X.X │.X..XX.X..XX.X.X │
│X...............X│....XXX........  │......XXXXXX...  │XXXXXXXXXXXXXXXX │XXXX........XXXX │.X..XX.XXX.XX..X │.X.XX..XX.XX.X.X │
│X...............X│...............  │...XXXXXXXXXXX.  │XXXXXXXXXXXXXXXX │XXXX........XXXX │.X.XXX..X..XXX.X │.X....XXXXXX.X.X │
│X...............X│...............  │..XX.........XX  │XXXXXXXXXXXXXXXX │XXX..........XXX │.X.X.XXX.XXX.X.X │.X.XXXXXXXXXXX.X │
│X...............X│                 │..XX.........XX  │XXXXXXXXXXXXXXXX │XXX..........XXX │.X.X...XXX...X.X │..XX.........XX. │
│X...............X│                 │...XXXXXXXXXXX.  │XXXXXXXXXXXXXXXX │XXX..........XXX │.X.X.XXX.XXX.X.X │..XX.........XX. │
│X...............X│                 │...............  │XXXXXXXXXXXXXXXX │XXX..........XXX │.X.XXX.X.X.XXX.X │.X.XXXXXXXXXXX.X │
│X...............X│                 │                 │XXXXXXXXXXXXXXXX │XXXXXXXXXXXXXXXX │.X..XX.X.X.XX..X │.X.X.X.X.X.X.X.X │
│X...............X│                 │                 │                 │                 │                 │                 │
│XXXXXXXXXXXXXXXXX│                 │                 │                 │                 │                 │                 │
├─────────────────┼─────────────────┼─────────────────┼─────────────────┼─────────────────┼─────────────────┼─────────────────┤
│.......XX.....+  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.* │.....XX*XXXX...  │                 │                 │                 │
│.....XX..X.....  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │.......XXX.....  │                 │                 │                 │
│...XX...XX.....  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │...XXXXXXXXXXX.  │                 │                 │                 │
│.......XXXX....  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │..XXXXXXXXXXXXX  │                 │                 │                 │
│.....XXXXXXXX..  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │..XXXXXXXXXXXXX  │                 │                 │                 │
│....XXX...XXXX.  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │..XXXXXXXXXXXXX  │                 │                 │                 │
│...XXXX.XX.XXXX  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │...............  │                 │                 │                 │
│...XXXX.XX.XXXX  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│...XXXX.XX.XXXX  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│...XXXX.XX.XXXX  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│....XXX...XXXX.  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│.....XXXXXXXX..  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│.......XXXX....  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│...............  │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│                 │..X.X.X.X.X.X.X  │.X.X.X.X.X.X.X.X │                 │                 │                 │                 │
│                 │                 │                 │                 │                 │                 │                 │
│                 │                 │                 │                 │                 │                 │                 │
└─────────────────┴─────────────────┴─────────────────┴─────────────────┴─────────────────┴─────────────────┴─────────────────┘
```

## Graphics extract

```shell
$ st --debug extract --shape 3 --output robot.png --format png --border box ~/Downloads/shapes/NEW\ MOUSE
```

![Shape #3](images/robot.png "Robot") 

```shell
$ st --debug extract --output=new-mouse-shapes.png --border=box --skip-empty --format=png ~/Downloads/shapes/NEW\ MOUSE
```
![All shapes](images/new-mouse-shapes.png "All shapes") 

## Shape generation

```shell
$ st generate --stdout api/src/test/resources/box-longform.st | st extract --stdin --stdout
+-----+
|.XXX.|
|X...X|
|X.+.X|
|X...X|
|.XXX.|
+-----+
```
