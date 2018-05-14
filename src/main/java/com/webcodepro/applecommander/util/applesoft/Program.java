package com.webcodepro.applecommander.util.applesoft;

import java.util.ArrayList;
import java.util.List;

/** A Program is a series of lines. */
public class Program {
	public final List<Line> lines = new ArrayList<>();
	
	public Program accept(Visitor t) {
		return t.visit(this);
	}
}
