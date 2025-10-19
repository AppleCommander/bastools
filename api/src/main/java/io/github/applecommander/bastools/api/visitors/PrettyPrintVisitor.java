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
package io.github.applecommander.bastools.api.visitors;

import java.io.PrintStream;

import io.github.applecommander.bastools.api.Visitor;
import io.github.applecommander.bastools.api.Visitors.PrintBuilder;
import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;

public class PrettyPrintVisitor implements Visitor {
	private final PrintStream printStream;
	
	public PrettyPrintVisitor(PrintBuilder builder) {
		this.printStream = builder.getPrintStream();
	}
	
	@Override
	public Line visit(Line line) {
		boolean first = true;
		for (Statement statement : line.statements) {
			if (first) {
				first = false;
				printStream.printf("%5d ", line.lineNumber);
			} else {
				printStream.printf("%5s ", ":");
			}
			statement.accept(this);
			printStream.println();
		}
		return line;
	}
	@Override
	public Token visit(Token token) {
		switch (token.type) {
		case EOL:
			printStream.print("<EOL>");
			break;
		case COMMENT:
			printStream.printf(" REM %s", token.text);
			break;
		case STRING:
			printStream.printf("\"%s\"", token.text);
			break;
		case KEYWORD:
			printStream.printf(" %s ", token.keyword.text);
			break;
		case IDENT:
		case SYNTAX:
			printStream.print(token.text);
			break;
		case DIRECTIVE:
			printStream.printf("%s ", token.text);
			break;
		case NUMBER:
			if (Math.rint(token.number) == token.number) {
				printStream.print(token.number.intValue());
			} else {
				printStream.print(token.number);
			}
			break;
		}
		return token;
	}
}
