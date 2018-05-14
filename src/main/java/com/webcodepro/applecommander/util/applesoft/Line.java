package com.webcodepro.applecommander.util.applesoft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** An AppleSoft BASIC Line representation. */
public class Line implements Consumer<Visitor> {
	public final int lineNumber;
	public final List<Statement> statements = new ArrayList<>();
	
	public Line(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	@Override
	public void accept(Visitor t) {
		t.visit(this);
	}
}
