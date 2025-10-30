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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Token.Type;

public class VariableReportVisitor implements Visitor {
	private final Map<String,SortedSet<Integer>> refs = new HashMap<>();
	private int currentLineNumber = -1;
	
	@Override
	public Program visit(Program program) {
		Program p = Visitor.super.visit(program);
		refs.entrySet().stream()
			.sorted(Map.Entry.comparingByKey())
			.forEach(this::print);
		return p;
	}
	private void print(Map.Entry<String,SortedSet<Integer>> e) {
		System.out.printf("%-8s  ", e.getKey());
		int c = 0;
		for (int i : e.getValue()) {
			if (c > 0) System.out.print(", ");
			if (c > 0 && c % 10 == 0) System.out.printf("\n          ");
			System.out.print(i);
			c += 1;
		}
		System.out.println();
	}
	
	@Override
	public Line visit(Line line) {
		currentLineNumber = line.lineNumber;
		return Visitor.super.visit(line);
	}
	
	@Override
	public Token visit(Token token) {
		if (token.type() == Type.IDENT) {
			refs.merge(token.text(),
					new TreeSet<>(Arrays.asList(currentLineNumber)), 
					(a,b) -> { a.addAll(b); return a; });
		}
		return Visitor.super.visit(token);
	}
}