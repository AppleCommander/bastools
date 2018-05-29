package io.github.applecommander.bastokenizer.api;

import io.github.applecommander.bastokenizer.api.model.Line;
import io.github.applecommander.bastokenizer.api.model.Program;
import io.github.applecommander.bastokenizer.api.model.Statement;
import io.github.applecommander.bastokenizer.api.model.Token;

/**
 * The Visitor interface allows some flexibility in what can be done with the
 * AppleSoft BASIC program code.
 *  
 * @author rob
 * @see Visitors
 */
public interface Visitor {
	default public Program visit(Program program) {
		program.lines.forEach(l -> l.accept(this));
		return program;
	}
	default public Line visit(Line line) {
		line.statements.forEach(s -> s.accept(this));
		return line;
	}
	default public Statement visit(Statement statement) {
		statement.tokens.forEach(t -> t.accept(this));
		return statement;
	}
	default public Token visit(Token token) {
		return token;
	};
}
