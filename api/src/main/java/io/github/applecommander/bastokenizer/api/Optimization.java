package io.github.applecommander.bastokenizer.api;

import java.util.function.Function;

import io.github.applecommander.bastokenizer.api.optimizations.MergeLines;
import io.github.applecommander.bastokenizer.api.optimizations.RemoveEmptyStatements;
import io.github.applecommander.bastokenizer.api.optimizations.RemoveRemStatements;
import io.github.applecommander.bastokenizer.api.optimizations.Renumber;

public enum Optimization {
	REMOVE_EMPTY_STATEMENTS(RemoveEmptyStatements::new),
	REMOVE_REM_STATEMENTS(RemoveRemStatements::new),
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
