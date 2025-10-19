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
