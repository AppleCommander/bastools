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

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

import org.applecommander.bastools.api.model.*;
import org.applecommander.bastools.api.model.Token.Type;

/** 
 * The Parser will read a series of Tokens and build a Program.
 * Note that this is not a compiler and does not "understand" the program. 
 */
public class Parser {
	private final Queue<Token> tokens;
	
	public Parser(Queue<Token> tokens) {
		Objects.requireNonNull(tokens);
		this.tokens = tokens;
	}
	
	public Program parse() {
		Program program = new Program();
		while (!tokens.isEmpty()) {
		    readLine(program).ifPresent(program.lines::add);
		}
		return program;
	}
	
	public Optional<Line> readLine(Program program) {
	    return expectNumber().map(lineNumber -> {
            Line line = new Line(lineNumber, program);
            while (!tokens.isEmpty() && tokens.peek().type() != Type.EOL) {
                Statement statement = readStatement();
                if (statement != null) {
                    line.statements.add(statement);
                } else {
                    break;
                }
            }
            if (!tokens.isEmpty() && tokens.peek().type() == Type.EOL) {
                tokens.remove();    // Skip that EOL
            }
            return line;
	    });
	}
	
	public Statement readStatement() {
		Statement statement = new Statement();
        Token firstToken = null;
		while (!tokens.isEmpty()) {
			if (tokens.peek().type() == Type.EOL) break;
			Token t = tokens.remove();
			if (t.type() == Type.SYNTAX && ":".equals(t.text())) break;
            if (firstToken == null) {
                firstToken = t;
            }
            else if (firstToken.keyword() == ApplesoftKeyword.DATA && t.keyword() != null) {
                // AppleSoft doesn't put actual keyword or tokens into data (beyond quotes or comma)
                t = Token.ident(t.line(), t.keyword().text);
            }
			statement.tokens.add(t);
		}
		return statement;
	}
	
	public Optional<Integer> expectNumber() {
		Token c = tokens.remove();
		if (c.type() == Type.EOL) {
		    return Optional.empty();
		}
		if (c.type() != Type.NUMBER) {
			throw new RuntimeException("Expected a number in line #" + c.line());
		}
		return Optional.of(c.number().intValue());
	}
}
