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
package io.github.applecommander.bastools.api.optimizations;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.model.Line;

/**
 * A simple renumbering algorithm that maps the reassignments and lets {@code BaseVisitor}
 * perform the actual renumbering!
 */
public class Renumber extends BaseVisitor {
	protected int lineNumber = 0;
	
	public Renumber(Configuration config) {
		// ignored
	}
	
	@Override
	public Line visit(Line line) {
		Line newLine = new Line(lineNumber++, this.newProgram);
		newLine.statements.addAll(line.statements);
		// Track what went where so lines can get renumbered automatically
		reassignments.put(line.lineNumber, newLine.lineNumber);
		return newLine;
	}
}
