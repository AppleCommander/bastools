package io.github.applecommander.bastools.api.optimizations;

import java.io.PrintStream;
import java.util.Set;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Visitors;
import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;
import io.github.applecommander.bastools.api.visitors.ByteVisitor;
import io.github.applecommander.bastools.api.visitors.LineNumberTargetCollector;

public class MergeLines extends BaseVisitor {
	private Set<Integer> targets;
	private Line mergeLine;
	private final ByteVisitor bv;
	private final int maxLineLength;
	private final PrintStream debugStream;
	
	public MergeLines(Configuration config) {
		this.maxLineLength = config.maxLineLength;
		this.debugStream = config.debugStream;
		this.bv = Visitors.byteVisitor(config);
	}
	
	@Override
	public Program visit(Program program) {
		LineNumberTargetCollector c = Visitors.lineNumberTargetCollector();
		program.accept(c);
		targets = c.getTargets();
		debugStream.printf("Target lines = %s\n", targets);
		return super.visit(program);
	}
	
	@Override
	public Line visit(Line line) {
		debugStream.printf("Line # %d : ", line.lineNumber);
		Line newLine = new Line(line.lineNumber, this.newProgram);
		newLine.statements.addAll(line.statements);
		if (mergeLine == null || targets.contains(line.lineNumber)) {
			// Either forced to a new line or this is a GOTO type target: Ignore length
			debugStream.printf("%s\n", mergeLine == null ? "mergeLine is null" : "target line #");
		} else {
			// Check length and decide if it merges based on that.
			Line tmpLine = new Line(mergeLine.lineNumber, mergeLine.program);
			tmpLine.statements.addAll(mergeLine.statements);
			tmpLine.statements.addAll(line.statements);
			if (bv.length(tmpLine) > maxLineLength) {
				// It was too big, do not add
				debugStream.printf("merge would exceed max line length: %d > %d\n", bv.length(tmpLine), maxLineLength);
			} else {
				// We can add line to mergeLine (mergeLine is already added to program, must keep that object)
				mergeLine.statements.addAll(line.statements);
				if (hasTerminal(line)) mergeLine = null;
				debugStream.printf("line %s\n", mergeLine == null ? "had terminals" : "was added to mergeLine");
				return null;
			}
		}
		// Always reset mergeLine based on the terminal characteristics
		mergeLine = hasTerminal(line) ? null : newLine;
		debugStream.printf("line %s\n", mergeLine == null ? "had terminals" : "is now mergeLine");
		return newLine;
	}
	
	private boolean hasTerminal(Line line) {
		// Terminals are: IF, REM, GOTO, END, ON .. GOTO (GOTO is trigger), RESUME, RETURN, STOP
		// Includes directives.
		for (Statement s : line.statements) {
			for (Token t : s.tokens) {
				boolean terminal = t.keyword == ApplesoftKeyword.IF || t.type == Type.COMMENT /* REM */
						|| t.keyword == ApplesoftKeyword.GOTO || t.keyword == ApplesoftKeyword.END
						|| t.keyword == ApplesoftKeyword.RESUME || t.keyword == ApplesoftKeyword.RETURN
						|| t.keyword == ApplesoftKeyword.STOP
						|| t.type == Type.DIRECTIVE;
				if (terminal) return true;
			}
		}
		return false;
	}
}
