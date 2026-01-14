# Notes on code "proofers"

> Please correct or add to this information!

Some summary notes and observations on the Apple II code checking tools.

## Applesoft

|                                | Includes REM contents | Includes spaces | Control Chars? | Other? | Case-sensitive? | Operation    | Grouping(s)                     | Checksum Size                       | Program Total                        |
|--------------------------------|:----------------------|:----------------|:---------------|--------|:----------------|:-------------|:--------------------------------|:------------------------------------|:-------------------------------------|
| Compute! Automatic Proofreader | Yes                   | No              | CTRL+D         | N/A    | Yes             | Input Buffer | Line                            | 1 byte                              | No                                   |
| Nibble Checkit                 | No                    | In strings      | In Strings     | "?"    | Yes             | Input Buffer | Line, Program                   | 1 byte (line)<br/>2 bytes (program) | Yes, on line numbers                 | 
| Nibble Apple Checker           | Yes                   | No              | CTRL+D         | N/A    | Yes             | Program      | Program                         | 1 byte (2 bytes length)             | Yes, checksum and length calculation |
| MicroSPARC Key Perfect 2       | Yes                   | Yes             | All            | N/A    | Yes             | Program      | Groups of 10 lines,<br/>Program | Max 3 bytes                         | Yes, byte count                      | 
| MicroSPARC Key Perfect 4       | Yes                   | No              | CTRL+D         | N/A    | Yes             | Program      | Groups of 10 lines,<br/>Program | Max 3 bytes                         | Yes, byte count                      | 
| MicroSPARC Key Perfect 5       | No                    | No              | CTRL+D         | N/A    | No              | Program      | Groups of 10 lines,<br/>Program | 4 bytes                             | Yes                                  |

* *Includes REM contents* - most checksum algorithms includes the REM as well as the contents of the REM statement; some only include the REM itself.
* *Includes spaces* - indicates if whitespace is important.
* *Control Chars?* - indicates if any control characters are included in the checksum.
* *Other?* - Checkit replaces "?" with "PRINT" to standardize the shortcut for the `PRINT` statement. The catch is that it ignores quotes. So _any_ "?" is replaced with "PRINT" for the checksum algorithm. 
* *Case-sensitive?* - is "A" and "a" handled differently?
* *Operation* - if "Input Buffer" is specified, the checksums are displayed after a line of code has been entered; otherwise, the program must be completed and saved before running checksum.
* *Groupings* - what, if any, groupings apply.

## Binary

|                                | Includes spaces | Other? |
|--------------------------------|-----------------|--------|
| Compute! Automatic Proofreader |                 |        |
| Nibble Checkit                 | Yes             | Yes    | 
| Nibble Apple Checker           | No              | N/A    |
| MicroSPARC Key Perfect 2       | No              | N/A    | 
| MicroSPARC Key Perfect 4       |                 |        | 
| MicroSPARC Key Perfect 5       |                 |        |

* *Includes spaces* - indicates if spaces are important. For instance, Nibble Checkit uses the input buffer and does not ignore spaces. Thus, if extra spaces are includes like "0300:A9<spc><spc>00", the checksum will be off.
* *Other?* - Checkit does the program total against the program bytes, not the entered text. Only the line checksum uses the input buffer.

## References

* See the [reference](.) directory for source code and original articles.
* [Computer Magzine Proofreaders](https://github.com/22samurai/computer-magazine-proofreaders).
* [a2_proofreader](https://github.com/hartze11/a2_proofreader)
* As always searching through [archive.org](https://archive.org) is highly valuable.
