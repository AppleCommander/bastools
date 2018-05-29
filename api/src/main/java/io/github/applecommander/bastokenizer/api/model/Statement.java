package io.github.applecommander.bastokenizer.api.model;

import java.util.ArrayList;
import java.util.List;

import io.github.applecommander.bastokenizer.api.Visitor;

/** A Statement is simply a series of Tokens. */
public class Statement {
	public final List<Token> tokens = new ArrayList<>();
	
	public Statement accept(Visitor t) {
		return t.visit(this);
	}
}
