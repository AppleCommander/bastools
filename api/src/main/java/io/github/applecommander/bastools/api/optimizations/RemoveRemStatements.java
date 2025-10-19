package io.github.applecommander.bastools.api.optimizations;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token.Type;

/** Drop all REM statements as they are encountered in the tree walk. */
public class RemoveRemStatements extends BaseVisitor {
	public RemoveRemStatements(Configuration config) {
		// ignored
	}
	
	@Override
	public Statement visit(Statement statement) {
		return statement.tokens.getFirst().type == Type.COMMENT ? null : statement;
	}
}
