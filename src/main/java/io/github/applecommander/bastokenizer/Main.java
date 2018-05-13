package io.github.applecommander.bastokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Queue;
import java.util.concurrent.Callable;

import com.webcodepro.applecommander.util.applesoft.Parser;
import com.webcodepro.applecommander.util.applesoft.Program;
import com.webcodepro.applecommander.util.applesoft.Token;
import com.webcodepro.applecommander.util.applesoft.TokenReader;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** A command-line interface to the AppleSoft BAS tokenizer libraries. */
@Command(description = "Transforms an AppleSoft program from text back to it's tokenized state.",
		name = "bt", mixinStandardHelpOptions = true, 
		versionProvider = Main.VersionProvider.class)
public class Main implements Callable<Void> {
	@Option(names = { "-o", "--output" }, description = "Write binary output to file.")
	private File outputFile;
	
	@Option(names = { "-x", "--hex"}, description = "Generate a binary hex dump for debugging.")
	private boolean hexFormat;

	@Option(names = { "-c", "--copy"}, description = "Generate a copy/paste form of output for testing in an emulator.")
	private boolean copyFormat;
	
	@Option(names = { "-a", "--address" }, description = "Base address for program", showDefaultValue = Visibility.ALWAYS, converter = IntegerTypeConverter.class)
	private int address = 0x801;
	
	@Option(names = { "-p", "--pipe" }, description = "Pipe binary output to stdout.")
	private boolean pipeOutput;
	
	@Option(names = "--pretty", description = "Pretty print structure as bastokenizer understands it.")
	private boolean prettyPrint;

	@Option(names = "--tokens", description = "Dump token list to stdout for debugging.")
	private boolean showTokens;

	@Parameters(index = "0", description = "AppleSoft BASIC program to process.")
	private File sourceFile;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		CommandLine.call(new Main(), args);
	}
	
	@Override
	public Void call() throws FileNotFoundException, IOException {
		if (checkParameters()) {
			process();
		}
		
		return null;		// To satisfy object "Void"
	}
	
	/** A basic test to ensure parameters are somewhat sane. */
	public boolean checkParameters() {
		if (pipeOutput && (hexFormat || copyFormat || prettyPrint || showTokens)) {
			System.err.println("The pipe option blocks any other stdout options.");
			return false;
		} else if (!(pipeOutput || hexFormat || copyFormat || prettyPrint || showTokens || outputFile != null)) {
			System.err.println("What do you want to do?");
			return false;
		}
		return true;
	}
	
	/** General CLI processing. */
	public void process() throws FileNotFoundException, IOException {
		Queue<Token> tokens = TokenReader.tokenize(sourceFile);
		if (showTokens) {
			System.out.println(tokens.toString());
		}
		Parser parser = new Parser(tokens);
		Program program = parser.parse();
		if (prettyPrint) {
			program.prettyPrint(System.out);
		}
		
		byte[] data = program.toBytes(address);
		if (hexFormat) {
			hexDump(address, data, false);
		}
		if (copyFormat) {
			hexDump(address, data, true);
		}
		if (outputFile != null) {
			Files.write(outputFile.toPath(), data);
		}
		if (pipeOutput) {
			System.out.write(data);
		}
	}
	
	/** Dump data to stdout in various formats. */
	public void hexDump(int address, byte[] data, boolean forApple) {
		final int line = 16;
		int offset = 0;
		if (forApple) {
			int end = address + data.length;
			System.out.printf("0067: %02x %02x %02x %02x\n", address&0xff, address>>8, end&0xff, end>>8);
			System.out.printf("%04x: 00\n", address-1);
		}
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

	/** Display version information.  Note that this is dependent on Maven configuration. */
	public static class VersionProvider implements IVersionProvider {
        public String[] getVersion() {
        	return new String[] { Main.class.getPackage().getImplementationVersion() };
        }
	}
	/** Add support for "$801" and "0x801" instead of just decimal like 2049. */
	public static class IntegerTypeConverter implements ITypeConverter<Integer> {
		@Override
		public Integer convert(String value) throws Exception {
			if (value == null) {
				return null;
			} else if (value.startsWith("$")) {
				return Integer.valueOf(value.substring(1), 16);
			} else if (value.startsWith("0x") || value.startsWith("0X")) {
				return Integer.valueOf(value.substring(2), 16);
			} else {
				return Integer.valueOf(value);
			}
		}
	}
}
