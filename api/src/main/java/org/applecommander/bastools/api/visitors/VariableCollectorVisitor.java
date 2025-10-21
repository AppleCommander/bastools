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
package org.applecommander.bastools.api.visitors;

import java.util.HashSet;
import java.util.Set;

import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Token.Type;

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