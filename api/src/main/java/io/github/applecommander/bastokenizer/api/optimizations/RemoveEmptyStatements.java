package io.github.applecommander.bastokenizer.api.optimizations;

import io.github.applecommander.bastokenizer.api.model.Statement;

public class RemoveEmptyStatements extends BaseVisitor {
	@Override
	public Statement visit(Statement statement) {
		return statement.tokens.isEmpty() ? null : statement;
	}
}
