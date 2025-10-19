package io.github.applecommander.bastools.api.visitors;

import java.util.HashSet;
import java.util.Set;

import io.github.applecommander.bastools.api.Visitor;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;

public class VariableCollectorVisitor implements Visitor {
	private final Set<String> variableNames = new HashSet<>();
	
	public Set<String> getVariableNames() {
		return this.variableNames;
	}
	
	@Override
	public Token visit(Token token) {
		if (token.type == Type.IDENT) {
			variableNames.add(token.text);
		}
		return Visitor.super.visit(token);
	}
}