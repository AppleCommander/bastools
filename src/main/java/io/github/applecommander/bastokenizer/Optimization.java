package io.github.applecommander.bastokenizer;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.webcodepro.applecommander.util.applesoft.ApplesoftKeyword;
import com.webcodepro.applecommander.util.applesoft.Line;
import com.webcodepro.applecommander.util.applesoft.Program;
import com.webcodepro.applecommander.util.applesoft.Statement;
import com.webcodepro.applecommander.util.applesoft.Token;
import com.webcodepro.applecommander.util.applesoft.Token.Type;
import com.webcodepro.applecommander.util.applesoft.Visitor;
import com.webcodepro.applecommander.util.applesoft.Visitors;
import com.webcodepro.applecommander.util.applesoft.Visitors.LineNumberTargetCollector;

import picocli.CommandLine.ITypeConverter;

public enum Optimization {
	REMOVE_EMPTY_STATEMENTS(new BaseVisitor() {
		@Override
		public Statement visit(Statement statement) {
			return statement.tokens.isEmpty() ? null : statement;
		}
	}),
	REMOVE_REM_STATEMENTS(new BaseVisitor() {
		@Override
		public Statement visit(Statement statement) {
			return statement.tokens.get(0).type == Type.COMMENT ? null : statement;
		}
	}),
	MERGE_LINES(new BaseVisitor() {
		private Set<Integer> targets;
		private Line mergeLine;
		@Override
		public Program visit(Program program) {
			LineNumberTargetCollector c = Visitors.lineNumberTargetCollector();
			program.accept(c);
			targets = c.getTargets();
			return super.visit(program);
		}
		@Override
		public Line visit(Line line) {
			if (mergeLine == null || targets.contains(line.lineNumber)) {
				// merge may null out mergeLine if the this line has a "terminal".  
				// Preserve it with newLine so it get added to the program.
				Line newLine = new Line(line.lineNumber);
				mergeLine = newLine;
				merge(line);
				return newLine;
			}
			merge(line);
			// Do not preserve old line!
			return null;
		}
		private void merge(Line line) {
			mergeLine.statements.addAll(line.statements);
			// Terminals are: IF, REM, GOTO, END, ON .. GOTO (GOTO is trigger), RESUME, RETURN, STOP
			boolean terminal = false;
			for (Statement s : line.statements) {
				for (Token t : s.tokens) {
					terminal |= t.keyword == ApplesoftKeyword.IF || t.type == Type.COMMENT /* REM */
							|| t.keyword == ApplesoftKeyword.GOTO || t.keyword == ApplesoftKeyword.END
							|| t.keyword == ApplesoftKeyword.RESUME || t.keyword == ApplesoftKeyword.RETURN
							|| t.keyword == ApplesoftKeyword.STOP;
				}
			}
			if (terminal) {
				mergeLine = null;
			}
		}
	}),
	RENUMBER(new BaseVisitor() {
		protected int lineNumber = 0;
		@Override
		public Line visit(Line line) {
			Line newLine = new Line(lineNumber++);
			newLine.statements.addAll(line.statements);
			// Track what went where so lines can get renumbered automatically
			reassignments.put(line.lineNumber, newLine.lineNumber);
			return newLine;
		}
	})
	;
	
	public final Visitor visitor;
	
	private Optimization(Visitor visitor) {
		this.visitor = visitor;
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
		@Override
		public Program visit(Program program) {
			final Program newProgram = new Program();
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
			Line newLine = new Line(line.lineNumber);
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
}
