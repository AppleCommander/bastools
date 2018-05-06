package net.sf.applecommander.bastokenizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Program {
	public final List<Line> lines = new ArrayList<>();
	
	public void prettyPrint(PrintStream ps) {
		for (Line line : lines) {
			line.prettyPrint(ps);
		}
	}
}
