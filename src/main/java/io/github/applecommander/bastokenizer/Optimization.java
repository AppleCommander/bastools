package io.github.applecommander.bastokenizer;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.webcodepro.applecommander.util.applesoft.ApplesoftKeyword;
import com.webcodepro.applecommander.util.applesoft.Line;
import com.webcodepro.applecommander.util.applesoft.Program;
import com.webcodepro.applecommander.util.applesoft.Statement;
import com.webcodepro.applecommander.util.applesoft.Token;
import com.webcodepro.applecommander.util.applesoft.Token.Type;
import com.webcodepro.applecommander.util.applesoft.Visitor;
import com.webcodepro.applecommander.util.applesoft.Visitors;
import com.webcodepro.applecommander.util.applesoft.Visitors.ByteVisitor;
import com.webcodepro.applecommander.util.applesoft.Visitors.LineNumberTargetCollector;

import picocli.CommandLine.ITypeConverter;

public enum Optimization {
	REMOVE_EMPTY_STATEMENTS(opts -> new RemoveEmptyStatements()),
	REMOVE_REM_STATEMENTS(opts -> new RemoveRemStatements()),
	MERGE_LINES(opts -> new MergeLines(opts)),
	RENUMBER(opts -> new Renumber())
	;
	
	private Function<Main,Visitor> factory;
	
	private Optimization(Function<Main,Visitor> factory) {
		this.factory = factory;
	}
	
	public Visitor create(Main options) {
		return factory.apply(options);
	}
	
	/** Add support for lower-case Optimization flags. */
	public static class TypeConverter implements ITypeConverter<Optimization> {
		@Override
		public Optimization convert(String value) throws Exception {
			try {
				return Optimization.valueOf(value);
			} catch (IllegalArgumentException ex) {
				for (Optimization opt : Optimization.values()) {
					String checkName = opt.name().replace('_', '-');
					if (checkName.equalsIgnoreCase(value)) {
						return opt;
					}
				}
				throw ex;
			}
		}
	}
	/** Common base class for optimization visitors that allow the program tree to be rewritten. */
	private static class BaseVisitor implements Visitor {
		protected Map<Integer,Integer> reassignments = new HashMap<>();
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
	private static class RemoveEmptyStatements extends BaseVisitor {
		@Override
		public Statement visit(Statement statement) {
			return statement.tokens.isEmpty() ? null : statement;
		}
	}
	private static class RemoveRemStatements extends BaseVisitor {
		@Override
		public Statement visit(Statement statement) {
			return statement.tokens.get(0).type == Type.COMMENT ? null : statement;
		}
	}
	private static class MergeLines extends BaseVisitor {
		private Set<Integer> targets;
		private Line mergeLine;
		private ByteVisitor bv = Visitors.byteVisitor(0x801);
		private int maxLineLength;
		private PrintStream debug;
		private MergeLines(Main options) {
			this.maxLineLength = options.maxLineLength;
			this.debug = options.debug;
		}
		@Override
		public Program visit(Program program) {
			LineNumberTargetCollector c = Visitors.lineNumberTargetCollector();
			program.accept(c);
			targets = c.getTargets();
			debug.printf("Target lines = %s\n", targets);
			return super.visit(program);
		}
		@Override
		public Line visit(Line line) {
			debug.printf("Line # %d : ", line.lineNumber);
			Line newLine = new Line(line.lineNumber, this.newProgram);
			newLine.statements.addAll(line.statements);
			if (mergeLine == null || targets.contains(line.lineNumber)) {
				// Either forced to a new line or this is a GOTO type target: Ignore length
				debug.printf("%s\n", mergeLine == null ? "mergeLine is null" : "target line #");
			} else {
				// Check length and decide if it merges based on that.
				Line tmpLine = new Line(mergeLine.lineNumber, mergeLine.program);
				tmpLine.statements.addAll(mergeLine.statements);
				tmpLine.statements.addAll(line.statements);
				if (bv.length(tmpLine) > maxLineLength) {
					// It was too big, do not add
					debug.printf("merge would exceed max line length: %d > %d\n", bv.length(tmpLine), maxLineLength);
				} else {
					// We can add line to mergeLine (mergeLine is already added to program, must keep that object)
					mergeLine.statements.addAll(line.statements);
					if (hasTerminal(line)) mergeLine = null;
					debug.printf("line %s\n", mergeLine == null ? "had terminals" : "was added to mergeLine");
					return null;
				}
			}
			// Always reset mergeLine based on the terminal characteristics
			mergeLine = hasTerminal(line) ? null : newLine;
			debug.printf("line %s\n", mergeLine == null ? "had terminals" : "is now mergeLine");
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
	private static class Renumber extends BaseVisitor {
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
}
