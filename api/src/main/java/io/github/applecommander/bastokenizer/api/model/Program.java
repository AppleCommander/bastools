package io.github.applecommander.bastokenizer.api.model;

import java.util.ArrayList;
import java.util.List;

import io.github.applecommander.bastokenizer.api.Visitor;

/** A Program is a series of lines. */
public class Program {
	public final List<Line> lines = new ArrayList<>();
	
	public Program accept(Visitor t) {
		return t.visit(this);
	}
}
