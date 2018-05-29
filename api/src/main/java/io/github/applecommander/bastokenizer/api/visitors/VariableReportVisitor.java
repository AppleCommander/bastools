package io.github.applecommander.bastokenizer.api.visitors;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import io.github.applecommander.bastokenizer.api.Visitor;
import io.github.applecommander.bastokenizer.api.model.Line;
import io.github.applecommander.bastokenizer.api.model.Program;
import io.github.applecommander.bastokenizer.api.model.Token;
import io.github.applecommander.bastokenizer.api.model.Token.Type;

public class VariableReportVisitor implements Visitor {
	private Map<String,SortedSet<Integer>> refs = new HashMap<>();
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
		if (token.type == Type.IDENT) {
			refs.merge(token.text, 
					new TreeSet<>(Arrays.asList(currentLineNumber)), 
					(a,b) -> { a.addAll(b); return a; });
		}
		return Visitor.super.visit(token);
	}
}