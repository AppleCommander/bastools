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
package org.applecommander.bastools.api.optimizations;

import java.util.HashMap;
import java.util.Map;

import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.Visitors;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;

/** 
 * Common base class for optimization visitors that allow the program tree to be rewritten.
 * Note that {@code #reassignments} is used to track line number movement and is <em>automatically</em>
 * applied at the end of the program visit. 
 */
public class BaseVisitor implements Visitor {
	protected final Map<Integer,Integer> reassignments = new HashMap<>();
	protected Program newProgram;
	
	@Override
	public Program visit(Program program) {
		newProgram = new Program();
		program.lines.forEach(l -> {
			Line line = l.accept(this);
			boolean lineKept = line != null && !line.statements.isEmpty();
			if (lineKept) {
				newProgram.lines.add(line);
				reassignments.replaceAll((k,v) -> v == null ? l.lineNumber : v);
			} else {
				// Make a place-holder for the reassignment; we'll patch it in once we find a line that sticks around.
				reassignments.put(l.lineNumber, null);
			}
		});
		if (!reassignments.isEmpty()) {
			// Now, renumber based on our findings!
			return newProgram.accept(Visitors.reassignVisitor(reassignments));
		} else {
			return newProgram;
		}
	}
	
	@Override
	public Line visit(Line line) {
		Line newLine = new Line(line.lineNumber, this.newProgram);
		line.statements.forEach(s -> {
			Statement statement = s.accept(this);
			if (statement != null) newLine.statements.add(statement);
		});
		return newLine;
	}
	
	@Override
	public Statement visit(Statement statement) {
		Statement newStatement = new Statement();
		statement.tokens.forEach(t -> { 
			Token token = t.accept(this);
			if (token != null) newStatement.tokens.add(token);
		});
		return newStatement;
	}
	
	@Override
	public Token visit(Token token) {
		return token;
	}
}
