package com.webcodepro.applecommander.util.applesoft;

import java.util.ArrayList;
import java.util.List;

/** A Statement is simply a series of Tokens. */
public class Statement {
	public final List<Token> tokens = new ArrayList<>();
	
	public Statement accept(Visitor t) {
		return t.visit(this);
	}
}
