package net.sf.applecommander.bastokenizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Queue;

public class Main {
	public static void main(String[] args) throws FileNotFoundException, IOException {
		if (args.length != 1) {
			System.err.println("Please include a file to work on.");
			System.exit(1);
		}

		Queue<Token> tokens = TokenReader.tokenize(args[0]);
		System.out.println(tokens.toString());
		Parser parser = new Parser(tokens);
		Program program = parser.parse();
		program.prettyPrint(System.out);
	}
}
