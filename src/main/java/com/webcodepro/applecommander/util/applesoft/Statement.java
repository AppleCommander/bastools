package com.webcodepro.applecommander.util.applesoft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A Statement is simply a series of Tokens. */
public class Statement implements Consumer<Visitor> {
	public final List<Token> tokens = new ArrayList<>();
	
	@Override
	public void accept(Visitor t) {
		t.visit(this);
	}
}
