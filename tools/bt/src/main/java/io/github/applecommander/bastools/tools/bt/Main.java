package io.github.applecommander.bastools.tools.bt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Optimization;
import io.github.applecommander.bastools.api.Parser;
import io.github.applecommander.bastools.api.TokenReader;
import io.github.applecommander.bastools.api.Visitors;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;
import io.github.applecommander.bastools.api.visitors.ByteVisitor;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** A command-line interface to the AppleSoft BAS tokenizer libraries. */
@Command(description = "Transforms an AppleSoft program from text back to its tokenized state.",
		descriptionHeading = "%n",
		commandListHeading = "%nCommands:%n",
		optionListHeading = "%nOptions:%n",
		name = "bt", mixinStandardHelpOptions = true, 
		versionProvider = VersionProvider.class)
public class Main implements Callable<Void> {
	private static final int BAS = 0xfc;
	
	@Option(names = { "-o", "--output" }, description = "Write binary output to file.")
	private File outputFile;
	
	@Option(names = { "-x", "--hex"}, description = "Generate a binary hex dump for debugging.")
	private boolean hexFormat;

	@Option(names = { "-c", "--copy"}, description = "Generate a copy/paste form of output for testing in an emulator.")
	private boolean copyFormat;
	
	@Option(names = { "-a", "--address" }, description = "Base address for program", showDefaultValue = Visibility.ALWAYS, converter = IntegerTypeConverter.class)
	private int address = 0x801;
	
	@Option(names = { "--variables" }, description = "Generate a variable report")
	private boolean showVariableReport;
	
	@Option(names = "--stdout", description = "Send binary output to stdout.")
	private boolean stdoutFlag;
	
	@Option(names = "--applesingle", description = "Write output in AppleSingle format")
	private boolean applesingleFlag;
	
	@Option(names = "--pretty", description = "Pretty print structure as bastools understands it.")
	private boolean prettyPrint;

	@Option(names = "--list", description = "List structure as bastools understands it.")
	private boolean listPrint;

	@Option(names = "--tokens", description = "Dump token list to stdout for debugging.")
	private boolean showTokens;
	
	@Option(names = "--addresses", description = "Dump line number addresses out.")
	private boolean showLineAddresses;
	
	@Option(names = "--max-line-length", description = "Maximum line length for generated lines.", showDefaultValue = Visibility.ALWAYS)
	private int maxLineLength = 255;
	
	@Option(names = "-f", converter = OptimizationTypeConverter.class, split = ",", description = {
			"Enable specific optimizations.",
			"* @|green remove-empty-statements|@ - Strip out all '::'-like statements.",
			"* @|green remove-rem-statements|@ - Remove all REM statements.",
			"* @|green extract-constant-values|@ - Assign all constant values first.",
			"* @|green merge-lines|@ - Merge lines.",
			"* @|green renumber|@ - Renumber program."
	})
	private List<Optimization> optimizations = new ArrayList<>();

	@Option(names = { "-O", "--optimize" }, description = "Apply all optimizations.")
	private boolean allOptimizations;
	
	@Option(names = "--debug", description = "Print debug output.")
	private static boolean debugFlag;
	private PrintStream debug = new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				// Do nothing
			}
		});
	
	@Parameters(index = "0", description = "AppleSoft BASIC program to process.")
	private File sourceFile;
	
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			CommandLine.call(new Main(), args);
		} catch (Throwable t) {
			if (Main.debugFlag) {
				t.printStackTrace(System.err);
			} else {
				String message = t.getMessage();
				while (t != null) {
					message = t.getMessage();
					t = t.getCause();
				}
				System.err.printf("Error: %s\n", Optional.ofNullable(message).orElse("An error occurred."));
			}
			System.exit(1);
		}
	}
	
	@Override
	public Void call() throws FileNotFoundException, IOException {
		if (checkParameters()) {
			Configuration.Builder builder = Configuration.builder()
					.maxLineLength(this.maxLineLength)
					.sourceFile(this.sourceFile)
					.startAddress(this.address);
			if (debugFlag) builder.debugStream(System.out);
			process(builder.build());
		}
		
		return null;		// To satisfy object "Void"
	}
	
	/** A basic test to ensure parameters are somewhat sane. */
	public boolean checkParameters() {
		if (allOptimizations) {
			optimizations.clear();
			optimizations.addAll(Arrays.asList(Optimization.values()));
		}
		boolean hasTextOutput = hexFormat || copyFormat || prettyPrint || listPrint || showTokens || showVariableReport 
				|| debugFlag || showLineAddresses;
		if (stdoutFlag && hasTextOutput) {
			System.err.println("The pipe option blocks any other stdout options.");
			return false;
		} else if (!(stdoutFlag || hasTextOutput || outputFile != null)) {
			System.err.println("What do you want to do?");
			return false;
		}
		return true;
	}
	
	/** General CLI processing. */
	public void process(Configuration config) throws FileNotFoundException, IOException {
		Queue<Token> tokens = TokenReader.tokenize(sourceFile);
		if (showTokens) {
			tokens.forEach(t -> System.out.printf("%s%s", t, t.type == Type.EOL ? "\n" : ", "));
		}
		Parser parser = new Parser(tokens);
		Program program = parser.parse();
		
		for (Optimization optimization : optimizations) {
			debug.printf("Optimization: %s\n", optimization.name());
			program = program.accept(optimization.create(config));
		}

		if (prettyPrint || listPrint) {
			program.accept(Visitors.printBuilder().prettyPrint(prettyPrint).build());
		}
		if (showVariableReport) {
			program.accept(Visitors.variableReportVisitor());
		}

		ByteVisitor byteVisitor = Visitors.byteVisitor(config);
		byte[] data = byteVisitor.dump(program);
		if (showLineAddresses) {
			byteVisitor.getLineAddresses().forEach((l,a) -> System.out.printf("%5d ... $%04x\n", l, a));
		}
		if (hexFormat) {
			HexDumper.standard().dump(address, data);
		}
		if (copyFormat) {
			HexDumper.apple2().dump(address, data);
		}
		
		saveResults(data);
	}
	
	public void saveResults(byte[] data) throws IOException {
		if (applesingleFlag) {
			String realName = null;
			if (sourceFile != null) {
				realName = sourceFile.getName().toUpperCase();
			} else if (outputFile != null) {
				realName = outputFile.getName().toUpperCase();
			} else {
				realName = "UNKNOWN";
			}
			if (realName.endsWith(".BAS")) {
				realName = realName.substring(0, realName.length()-4);
			}
			AppleSingle as = AppleSingle.builder()
					.auxType(address)
					.fileType(BAS)
					.dataFork(data)
					.realName(realName)
					.build();
			if (outputFile != null) {
				as.save(outputFile);
			}
			if (stdoutFlag) {
				as.save(System.out);
			}
		} else {
			if (outputFile != null) {
				Files.write(outputFile.toPath(), data);
			}
			if (stdoutFlag) {
				System.out.write(data);
			}
		}
	}
}
