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

import java.util.Set;
import java.util.TreeSet;

import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Token.Type;

public class LineNumberTargetCollector implements Visitor {
	private final Set<Integer> targets = new TreeSet<>();
	
	public Set<Integer> getTargets() {
		return targets;
	}
	
	/**
	 * We saw a trigger, collect any numbers that follow.
	 * 
	 * Trigger cases:
	 * - GOSUB n
	 * - GOTO n
	 * - IF ... THEN n
	 * - LIST n [ ,m ]
	 * - ON x GOTO n, m, ...
	 * - ON x GOSUB n, m, ...
	 * - ONERR GOTO n
	 * - RUN n
	 */
	@Override
	public Statement visit(Statement statement) {
		boolean next = false;
		boolean multiple = false;
		for (Token t : statement.tokens) {
			if (next) {
				if (t.type == Type.NUMBER) {
					targets.add(t.number.intValue());
				}
				next = multiple;	// preserve next based on if we have multiple line numbers or not.
			} else {
				next = t.keyword == ApplesoftKeyword.GOSUB || t.keyword == ApplesoftKeyword.GOTO 
					|| t.keyword == ApplesoftKeyword.THEN || t.keyword == ApplesoftKeyword.RUN
					|| t.keyword == ApplesoftKeyword.LIST;
				multiple |= t.keyword == ApplesoftKeyword.LIST || t.keyword == ApplesoftKeyword.ON;
			}
		}
		return statement;
	}
}
