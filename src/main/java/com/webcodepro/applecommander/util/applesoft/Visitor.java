package com.webcodepro.applecommander.util.applesoft;

/**
 * The Visitor interface allows some flexibility in what can be done with the
 * AppleSoft BASIC program code.
 *  
 * @author rob
 * @see Visitors
 */
public interface Visitor {
	default public void visit(Program program) {
		program.lines.forEach(l -> l.accept(this));
	}
	default public void visit(Line line) {
		line.statements.forEach(s -> s.accept(this));
	}
	default public void visit(Statement statement) {
		statement.tokens.forEach(t -> t.accept(this));
	}
	public void visit(Token token);
}
