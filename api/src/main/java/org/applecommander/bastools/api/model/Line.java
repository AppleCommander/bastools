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
package org.applecommander.bastools.api.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.applecommander.bastools.api.Visitor;

/** An AppleSoft BASIC Line representation. */
public class Line {
	public final Program program;
	public final int lineNumber;
	public final List<Statement> statements = new ArrayList<>();
	
	public Line(int lineNumber, Program program) {
		Objects.requireNonNull(program);
		this.lineNumber = lineNumber;
		this.program = program;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	public Optional<Line> nextLine() {
		int i = program.lines.indexOf(this);
		if (i == -1 || i+1 >= program.lines.size()) {
			return Optional.empty();
		}
		return Optional.of(program.lines.get(i+1));
	}
	
	public Line accept(Visitor t) {
		return t.visit(this);
	}
}
