# Notes on code "proofers"

> Please correct or add to this information.

Some summary notes and observations on the Apple II code proofing tools.

|                                | Includes REM contents | Includes spaces | Control Chars? | Case sensitive? | Operation    | Grouping(s)                     | Size                                | Program Total                        |
|--------------------------------|:----------------------|:----------------|:---------------|:----------------|:-------------|:--------------------------------|:------------------------------------|:-------------------------------------|
| Compute! Automatic Proofreader | Yes                   | No              | CTRL+D         | Yes             | Input Buffer | Line                            | 1 byte                              | No                                   |
| Nibble Checkit                 | No                    | In strings      | In Strings     | Yes             | Input Buffer | Line, Program                   | 1 byte (line)<br/>2 bytes (program) | Yes, on line numbers                 | 
| Nibble Apple Checker           | Yes                   | No              | CTRL+D         | Yes             | Program      | Program                         | 1 byte (2 bytes length)             | Yes, checksum and length calculation |
| MicroSPARC Key Perfect 4       | Yes                   | No              | CTRL+D         | Yes             | Program      | Groups of 10 lines,<br/>Program | Max 3 bytes                         | Yes, byte count                      | 
| MicroSPARC Key Perfect 5       | No                    | No              | CTRL+D         | No              | Program      | Groups of 10 lines,<br/>Program | 4 bytes                             | Yes                                  |

## References

* See the [reference](.) directory for source code and original articles.
* [Computer Magzine Proofreaders](https://github.com/22samurai/computer-magazine-proofreaders).
* [a2_proofreader](https://github.com/hartze11/a2_proofreader)
* As always searching through [archive.org](https://archive.org) is highly valuable.
