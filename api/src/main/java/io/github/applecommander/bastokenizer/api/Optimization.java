package io.github.applecommander.bastokenizer.api;

import java.util.function.Function;

import io.github.applecommander.bastokenizer.api.optimizations.ExtractConstantValues;
import io.github.applecommander.bastokenizer.api.optimizations.MergeLines;
import io.github.applecommander.bastokenizer.api.optimizations.RemoveEmptyStatements;
import io.github.applecommander.bastokenizer.api.optimizations.RemoveRemStatements;
import io.github.applecommander.bastokenizer.api.optimizations.Renumber;

/**
 * All optimization capabilities are definined here in the "best" manner of execution.
 * Essentially, the goal is to prioritize the optimizations to manage dependencies.
 */
public enum Optimization {
	REMOVE_EMPTY_STATEMENTS(RemoveEmptyStatements::new),
	REMOVE_REM_STATEMENTS(RemoveRemStatements::new),
	EXTRACT_CONSTANT_VALUES(ExtractConstantValues::new),
	MERGE_LINES(MergeLines::new),
	RENUMBER(Renumber::new);
	
	private Function<Configuration,Visitor> factory;
	
	private Optimization(Function<Configuration,Visitor> factory) {
		this.factory = factory;
	}
	
	public Visitor create(Configuration config) {
		return factory.apply(config);
	}
	
}
