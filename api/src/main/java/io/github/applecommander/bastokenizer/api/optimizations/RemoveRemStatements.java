package io.github.applecommander.bastokenizer.api.optimizations;

import io.github.applecommander.bastokenizer.api.Configuration;
import io.github.applecommander.bastokenizer.api.model.Statement;
import io.github.applecommander.bastokenizer.api.model.Token.Type;

/** Drop all REM statements as they are encountered in the tree walk. */
public class RemoveRemStatements extends BaseVisitor {
	public RemoveRemStatements(Configuration config) {
		// ignored
	}
	
	@Override
	public Statement visit(Statement statement) {
		return statement.tokens.get(0).type == Type.COMMENT ? null : statement;
	}
}
