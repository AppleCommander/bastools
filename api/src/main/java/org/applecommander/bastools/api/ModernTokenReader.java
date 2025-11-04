/*
 * bastools
 * Copyright (C) 2025  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.applecommander.bastools.api;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Token;

/**
 * The TokenReader, given a text file, generates a series of Tokens (in the compiler sense, 
 * not AppleSoft) for the AppleSoft program.
 * <p/>
 * Note that this relies on the Java StreamTokenizer. The goal is to provide a more modern
 * parsing of tokens (in which it succeeds), however, AppleSoft itself is dated and mixes with
 * what a modern syntax would consist of. Specifically, the '#' is out of place as part of a
 * token and not a syntax element. Hence there is some funny business embedded in the code.
 * 
 * @author rob
 */
public class ModernTokenReader {
	private boolean hasMore = true;
	// Internal flag just in case we consume the EOL (see REM for instance)s
	private boolean needSyntheticEol = false;
	private final Reader reader;
	private final StreamTokenizer tokenizer;

	/** A handy method to generate a list of Tokens from a file name. */
	public static Queue<Token> tokenize(String filename) throws IOException {
		try (FileReader fileReader = new FileReader(filename)) {
			return tokenize(fileReader);
		}
	}
	/** A handy method to generate a list of Tokens from a file. */
	public static Queue<Token> tokenize(File file) throws IOException {
		try (FileReader fileReader = new FileReader(file)) {
			return tokenize(fileReader);
		}
	}
	/** A handy method to generate a list of Tokens from an InputStream. */
	public static Queue<Token> tokenize(InputStream inputStream) throws IOException {
		try (InputStreamReader streamReader = new InputStreamReader(inputStream)) {
			return tokenize(streamReader);
		}
	}
	private static Queue<Token> tokenize(Reader reader) throws IOException {
		ModernTokenReader tokenReader = new ModernTokenReader(reader);
		LinkedList<Token> tokens = new LinkedList<>();
		while (tokenReader.hasMore()) {
			// Magic number: maximum number of pieces from the StreamTokenizer that may be combined.
			tokenReader.next(2)
					   .ifPresent(tokens::add);
		}
		return tokens;
	}

	private ModernTokenReader(Reader reader) {
		this.reader = reader;
		this.tokenizer = ApplesoftKeyword.tokenizer(reader);
	}
	
	private boolean hasMore() {
		return hasMore;
	}
	
	private Optional<Token> next(int depth) throws IOException {
		// A cheesy attempt to prevent too much looping...
		if (depth > 0) {
			if (this.needSyntheticEol) {
				this.needSyntheticEol = false;
				int line = tokenizer.lineno();
				return Optional.of(Token.eol(line));
			}
			hasMore = tokenizer.nextToken() != StreamTokenizer.TT_EOF;
			if (hasMore) {
				int line = tokenizer.lineno();
				switch (tokenizer.ttype) {
				case StreamTokenizer.TT_EOL:
					return Optional.of(Token.eol(line));
				case StreamTokenizer.TT_NUMBER:
					return Optional.of(Token.number(line, tokenizer.nval));
				case StreamTokenizer.TT_WORD:
					Optional<ApplesoftKeyword> opt = ApplesoftKeyword.find(tokenizer.sval);
					// REM is special
					if (opt.filter(kw -> kw == ApplesoftKeyword.REM).isPresent()) {
						StringBuilder sb = new StringBuilder();
						while (true) {
							// Bypass the Tokenizer and just read to EOL for the comment
							int ch = reader.read();
							if (ch == '\n') {
								// Recover to the newline so that the next token is a EOL
								// This is needed for parsing!
								this.needSyntheticEol = true;
								break;
							}
							sb.append((char)ch);
						}
						return Optional.of(Token.comment(line, sb.toString()));
					}
					// If we found an Applesoft token, handle it
                    String sval = tokenizer.sval;
					if (opt.isPresent()) {
                        boolean good = true;
						if (opt.get().parts.size() > 1) {
							// Pull next token and see if it is the 2nd part ("PR#" == "PR", "#"; checking for the "#")
                            // If not, drop into identifier routine
							good = next(depth-1)
								.filter(t -> opt.get().parts.get(1).equals(t.text()))
                                .isPresent();
						}
                        if (good) {
                            ApplesoftKeyword outKeyword = opt.get();
                            // Special case - canonicalize '?' alternate form of 'PRINT'
                            if (opt.filter(kw -> kw == ApplesoftKeyword.questionmark).isPresent()) {
                                outKeyword = ApplesoftKeyword.PRINT;
                            }
                            return Optional.of(Token.keyword(line, outKeyword));
                        } else {
                            tokenizer.pushBack();
                        }
					}
					// Check if we found a directive
					if (sval.startsWith("$")) {
						return Optional.of(Token.directive(line, tokenizer.sval));
					}
					// Found an identifier (A, A$, A%).  Test if it is an array ('A(', 'A$(', 'A%(').
					tokenizer.nextToken();
					if (tokenizer.ttype == '(') {
						sval += (char)tokenizer.ttype;
					} else {
						tokenizer.pushBack();
					}
					return Optional.of(Token.ident(line, sval));
				case '"':
					return Optional.of(Token.string(line, tokenizer.sval));
				case '(':
				case ')':
				case ',':
				case ':':
				case '$':
				case '#':
				case ';':
				case '&':
				case '=':
				case '<':
				case '>':
				case '*':
				case '+':
				case '-':
				case '/':
				case '^':
					return Optional.of(
							ApplesoftKeyword.find(String.format("%c", tokenizer.ttype))
							   .map(kw -> Token.keyword(line, kw))
							   .orElse(Token.syntax(line, tokenizer.ttype)));
				case '\\':
				    // Special case: introducing a backslash to ignore the IMMEDIATELY following EOL
				    // If this does not occur, we simply fall through and fail.  That is intentional!
				    if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
				        // Consume the EOL and continue on our merry way
				        break;
				    }
				default:
				    String message = String.format("Unknown or unexpected character '%c'", tokenizer.ttype);
				    if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
				        message = String.format("Unknown or unexpected string '%s'", tokenizer.sval);
				    } else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
				        message = String.format("Unknown or unexpected number %f", tokenizer.nval);
				    } else if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
				        message = "Unexpected EOF";
				    } else if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
				        message = "Unexpected EOL";
				    }
				    message += String.format(" found on line #%d", tokenizer.lineno());
					throw new IOException(message);
				}
			}
		}
		return Optional.empty();
	}
}
