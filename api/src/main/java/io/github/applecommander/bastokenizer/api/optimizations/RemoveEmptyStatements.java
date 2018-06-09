package io.github.applecommander.bastokenizer.api.optimizations;

import io.github.applecommander.bastokenizer.api.Configuration;
import io.github.applecommander.bastokenizer.api.model.Statement;

/** Remove any empty statements during the tree walk. Effective removes double "::"'s. */
public class RemoveEmptyStatements extends BaseVisitor {
	public RemoveEmptyStatements(Configuration config) {
		// ignored
	}

	@Override
	public Statement visit(Statement statement) {
		return statement.tokens.isEmpty() ? null : statement;
	}
}
