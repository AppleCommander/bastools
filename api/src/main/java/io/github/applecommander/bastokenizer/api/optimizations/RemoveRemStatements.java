package io.github.applecommander.bastokenizer.api.optimizations;

import io.github.applecommander.bastokenizer.api.model.Statement;
import io.github.applecommander.bastokenizer.api.model.Token.Type;

public class RemoveRemStatements extends BaseVisitor {
	@Override
	public Statement visit(Statement statement) {
		return statement.tokens.get(0).type == Type.COMMENT ? null : statement;
	}
}
