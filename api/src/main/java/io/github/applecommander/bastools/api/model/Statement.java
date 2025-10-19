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
package io.github.applecommander.bastools.api.model;

import java.util.ArrayList;
import java.util.List;

import io.github.applecommander.bastools.api.Visitor;

/** A Statement is simply a series of Tokens. */
public class Statement {
	public final List<Token> tokens = new ArrayList<>();
	
	public Statement accept(Visitor t) {
		return t.visit(this);
	}
}
