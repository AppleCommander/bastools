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
package org.applecommander.bastools.api;

import java.util.function.Function;

import org.applecommander.bastools.api.optimizations.ExtractConstantValues;
import org.applecommander.bastools.api.optimizations.ShortenVariableNames;
import org.applecommander.bastools.api.optimizations.MergeLines;
import org.applecommander.bastools.api.optimizations.RemoveEmptyStatements;
import org.applecommander.bastools.api.optimizations.RemoveRemStatements;
import org.applecommander.bastools.api.optimizations.Renumber;

/**
 * All optimization capabilities are definined here in the "best" manner of execution.
 * Essentially, the goal is to prioritize the optimizations to manage dependencies.
 */
public enum Optimization {
	REMOVE_EMPTY_STATEMENTS(RemoveEmptyStatements::new),
	REMOVE_REM_STATEMENTS(RemoveRemStatements::new),
	SHORTEN_VARIABLE_NAMES(ShortenVariableNames::new),
	EXTRACT_CONSTANT_VALUES(ExtractConstantValues::new),
	MERGE_LINES(MergeLines::new),
	RENUMBER(Renumber::new);
	
	private final Function<Configuration,Visitor> factory;
	
	Optimization(Function<Configuration, Visitor> factory) {
		this.factory = factory;
	}
	
	public Visitor create(Configuration config) {
		return factory.apply(config);
	}
	
}
