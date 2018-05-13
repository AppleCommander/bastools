package io.github.applecommander.bastokenizer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Queue;

import com.webcodepro.applecommander.util.applesoft.Parser;
import com.webcodepro.applecommander.util.applesoft.Program;
import com.webcodepro.applecommander.util.applesoft.Token;
import com.webcodepro.applecommander.util.applesoft.TokenReader;

/** A simple driver for the tokenizer for a sample and rudimentary test. */
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
		
		int address = 0x801;
		byte[] data = program.toBytes(address);
		print(address, data, false);
		print(address, data, true);
	}
	
	public static void print(int address, byte[] data, boolean forApple) {
		final int line = 16;
		int offset = 0;
		while (offset < data.length) {
			System.out.printf("%04x: ", address);
			for (int i=0; i<line; i++) {
				if (offset < data.length) {
					System.out.printf("%02x ", data[offset]);
				} else if (!forApple) {
					System.out.printf(".. ");
				}
				offset++;
			}
			System.out.print(" ");
			if (!forApple) {
				offset -= line;
				for (int i=0; i<line; i++) {
					char ch = ' ';
					if (offset < data.length) {
						byte b = data[offset];
						ch = (b >= ' ') ? (char)b : '.';
					}
					System.out.printf("%c", ch);
					offset++;
				}
			}
			System.out.printf("\n");
			address += line;
		}

	}
}
