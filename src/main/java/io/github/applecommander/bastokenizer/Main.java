package io.github.applecommander.bastokenizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import com.webcodepro.applecommander.util.applesoft.Parser;
import com.webcodepro.applecommander.util.applesoft.Program;
import com.webcodepro.applecommander.util.applesoft.Token;
import com.webcodepro.applecommander.util.applesoft.TokenReader;
import com.webcodepro.applecommander.util.applesoft.Visitors;

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

	@Option(names = "--list", description = "List structure as bastokenizer understands it.")
	private boolean listPrint;

	@Option(names = "--tokens", description = "Dump token list to stdout for debugging.")
	private boolean showTokens;
	
	@Option(names = "-f", converter = Optimization.TypeConverter.class, split = ",", description = {
			"Enable specific optimizations.",
			"* @|green remove-empty-statements|@ - Strip out all '::'-like statements.",
			"* @|green remove-rem-statements|@ - Remove all REM statements.",
			"* @|green renumber|@ - Renumber program."
	})
	private List<Optimization> optimizations = new ArrayList<>();

	@Option(names = "-O", description = "Apply all optimizations.")
	private boolean allOptimizations;
	
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
		if (allOptimizations) {
			optimizations.clear();
			optimizations.addAll(Arrays.asList(Optimization.values()));
		}
		if (pipeOutput && (hexFormat || copyFormat || prettyPrint || listPrint || showTokens)) {
			System.err.println("The pipe option blocks any other stdout options.");
			return false;
		} else if (!(pipeOutput || hexFormat || copyFormat || prettyPrint || listPrint || showTokens || outputFile != null)) {
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
		
		for (Optimization optimization : optimizations) {
			program = program.accept(optimization.visitor);
		}

		if (prettyPrint || listPrint) {
			program.accept(Visitors.printBuilder().prettyPrint(prettyPrint).build());
		}

		byte[] data = Visitors.byteVisitor(address).dump(program);
		if (hexFormat) {
			HexDumper.standard().dump(address, data);
		}
		if (copyFormat) {
			HexDumper.apple2().dump(address, data);
		}
		if (outputFile != null) {
			Files.write(outputFile.toPath(), data);
		}
		if (pipeOutput) {
			System.out.write(data);
		}
	}
	
	/** A slightly-configurable reusable hex dumping mechanism. */
	public static class HexDumper {
		private PrintStream ps = System.out;
		private int lineWidth = 16;
		private BiConsumer<Integer,Integer> printHeader;
		private BiConsumer<Integer,byte[]> printLine;
		
		public static HexDumper standard() {
			HexDumper hd = new HexDumper();
			hd.printHeader = hd::emptyHeader;
			hd.printLine = hd::standardLine;
			return hd;
		}
		public static HexDumper apple2() {
			HexDumper hd = new HexDumper();
			hd.printHeader = hd::apple2Header;
			hd.printLine = hd::apple2Line;
			return hd;
		}
		
		public void dump(int address, byte[] data) {
			printHeader.accept(address, data.length);
			int offset = 0;
			while (offset < data.length) {
				byte[] line = Arrays.copyOfRange(data, offset, Math.min(offset+lineWidth,data.length));
				printLine.accept(address+offset, line);
				offset += line.length;
			}
		}
		
		public void emptyHeader(int address, int length) {
			// Do Nothing
		}
		public void apple2Header(int address, int length) {
			int end = address + length;
			printLine.accept(0x67, new byte[] { (byte)(address&0xff), (byte)(address>>8), (byte)(end&0xff), (byte)(end>>8) });
			printLine.accept(address-1, new byte[] { 0x00 });
		}
		
		public void standardLine(int address, byte[] data) {
			ps.printf("%04x: ", address);
			for (int i=0; i<lineWidth; i++) {
				if (i < data.length) {
					ps.printf("%02x ", data[i]);
				} else {
					ps.printf(".. ");
				}
			}
			ps.print(" ");
			for (int i=0; i<lineWidth; i++) {
				char ch = ' ';
				if (i < data.length) {
					byte b = data[i];
					ch = (b >= ' ') ? (char)b : '.';
				}
				ps.printf("%c", ch);
			}
			ps.printf("\n");
		}
		public void apple2Line(int address, byte[] data) {
			ps.printf("%04X:", address);
			for (byte b : data) ps.printf("%02X ", b);
			ps.printf("\n");
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
