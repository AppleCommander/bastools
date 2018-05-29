package io.github.applecommander.bastokenizer.api.optimizations;

import io.github.applecommander.bastokenizer.api.model.Line;

public class Renumber extends BaseVisitor {
	protected int lineNumber = 0;
	@Override
	public Line visit(Line line) {
		Line newLine = new Line(lineNumber++, this.newProgram);
		newLine.statements.addAll(line.statements);
		// Track what went where so lines can get renumbered automatically
		reassignments.put(line.lineNumber, newLine.lineNumber);
		return newLine;
	}
}
