package net.sf.applecommander.bastokenizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Line {
	public final int lineNumber;
	public final List<Statement> statements = new ArrayList<>();
	
	public Line(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public void prettyPrint(PrintStream ps) {
		boolean first = true;
		for (Statement statement : statements) {
			if (first) {
				first = false;
				ps.printf("%5d ", lineNumber);
			} else {
				ps.printf("%5s ", ":");
			}
			statement.prettyPrint(ps);
			ps.println();
		}
	}
}
