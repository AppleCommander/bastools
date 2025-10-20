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
package io.github.applecommander.bastools.api;

import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;

/**
 * The Visitor interface allows some flexibility in what can be done with the
 * AppleSoft BASIC program code.
 *  
 * @author rob
 * @see Visitors
 */
public interface Visitor {
	default Program visit(Program program) {
		program.lines.forEach(l -> l.accept(this));
		return program;
	}
	default Line visit(Line line) {
		line.statements.forEach(s -> s.accept(this));
		return line;
	}
	default Statement visit(Statement statement) {
		statement.tokens.forEach(t -> t.accept(this));
		return statement;
	}
	default Token visit(Token token) {
		return token;
	}
}
