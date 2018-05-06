package net.sf.applecommander.bastokenizer;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class Statement {
	public final List<Token> tokens = new ArrayList<>();
	
	public void prettyPrint(PrintStream ps) {
		for (Token token : tokens) {
			token.prettyPrint(ps);
		}
	}
}
