package io.github.applecommander.bastools.api.visitors;

import java.util.Set;
import java.util.TreeSet;

import io.github.applecommander.bastools.api.Visitor;
import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;

public class LineNumberTargetCollector implements Visitor {
	private Set<Integer> targets = new TreeSet<>();
	
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
