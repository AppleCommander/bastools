package com.webcodepro.applecommander.util.applesoft;

import java.util.ArrayList;
import java.util.List;

/** An AppleSoft BASIC Line representation. */
public class Line {
	public final int lineNumber;
	public final List<Statement> statements = new ArrayList<>();
	
	public Line(int lineNumber) {
		this.lineNumber = lineNumber;
	}
	
	public Line accept(Visitor t) {
		return t.visit(this);
	}
}
