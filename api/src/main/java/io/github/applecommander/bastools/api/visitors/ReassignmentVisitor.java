package io.github.applecommander.bastools.api.visitors;

import java.util.Map;

import io.github.applecommander.bastools.api.Visitor;
import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;

/** This is a mildly rewritable Visitor. */
public class ReassignmentVisitor implements Visitor {
	private final Map<Integer,Integer> reassignments;
	private Program newProgram;
	
	public ReassignmentVisitor(Map<Integer,Integer> reassignments) {
		this.reassignments = reassignments;
	}
	
	@Override
	public Program visit(Program program) {
		newProgram = new Program();
		program.lines.forEach(l -> {
			Line line = l.accept(this);
			newProgram.lines.add(line);
		});
		return newProgram;
	}
	@Override
	public Line visit(Line line) {
		Line newLine = new Line(line.lineNumber, this.newProgram);
		line.statements.forEach(s -> {
			Statement statement = s.accept(this);
			newLine.statements.add(statement);
		});
		return newLine;
	}
	/**
	 * We saw a trigger, reassign any numbers that follow.
	 * 
	 * Trigger cases:
	 * - GOSUB n
	 * - GOTO n
	 * - IF ... THEN n
	 * - IF ... THEN [GOTO|GOSUB] n
	 * - LIST n [ ,m ]
	 * - ON x GOTO n, m, ...
	 * - ON x GOSUB n, m, ...
	 * - ONERR GOTO n
	 * - RUN n
	 */
	@Override
	public Statement visit(Statement statement) {
		boolean trigger = false;
		boolean then = false;     // Special case: Immediately after THEN, a number triggers reassignment.
		Statement newStatement = new Statement();
		for (Token t : statement.tokens) {
			Token newToken = t;
			if (trigger || then) {
				if (t.type == Type.NUMBER && reassignments.containsKey(t.number.intValue())) {
					newToken = Token.number(t.line, reassignments.get(t.number.intValue()).doubleValue());
				}
				then = false;
			}
			if (!trigger) {
				trigger = t.keyword == ApplesoftKeyword.GOSUB || t.keyword == ApplesoftKeyword.GOTO 
                    || t.keyword == ApplesoftKeyword.LIST || t.keyword == ApplesoftKeyword.RUN;
				then = t.keyword == ApplesoftKeyword.THEN;
			}
			newStatement.tokens.add(newToken);
		}
		return newStatement;
	}
}
