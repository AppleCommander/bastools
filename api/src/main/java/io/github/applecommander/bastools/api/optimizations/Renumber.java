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
