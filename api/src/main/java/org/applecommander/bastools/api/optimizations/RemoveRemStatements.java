/*
 * bastools
 * Copyright (C) 2025  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.applecommander.bastools.api.optimizations;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token.Type;

/** Drop all REM statements as they are encountered in the tree walk. */
public class RemoveRemStatements extends BaseVisitor {
	public RemoveRemStatements(Configuration config) {
		// ignored
	}
	
	@Override
	public Statement visit(Statement statement) {
		return statement.tokens.getFirst().type() == Type.COMMENT ? null : statement;
	}
}
