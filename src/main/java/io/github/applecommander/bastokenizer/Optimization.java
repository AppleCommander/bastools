package io.github.applecommander.bastokenizer;

import java.util.HashMap;
import java.util.Map;

import com.webcodepro.applecommander.util.applesoft.Line;
import com.webcodepro.applecommander.util.applesoft.Program;
import com.webcodepro.applecommander.util.applesoft.Statement;
import com.webcodepro.applecommander.util.applesoft.Token;
import com.webcodepro.applecommander.util.applesoft.Token.Type;
import com.webcodepro.applecommander.util.applesoft.Visitor;
import com.webcodepro.applecommander.util.applesoft.Visitors;

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
		@Override
		public Program visit(Program program) {
			final Program newProgram = new Program();
			Map<Integer,Integer> reassignments = new HashMap<>();
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
