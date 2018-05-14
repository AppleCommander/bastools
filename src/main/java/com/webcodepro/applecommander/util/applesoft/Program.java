package com.webcodepro.applecommander.util.applesoft;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** A Program is a series of lines. */
public class Program implements Consumer<Visitor> {
	public final List<Line> lines = new ArrayList<>();
	
	@Override
	public void accept(Visitor t) {
		t.visit(this);
	}
}
