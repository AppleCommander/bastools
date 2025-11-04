/*
 * bastools
 * Copyright (C) 2025  Robert Greene
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.applecommander.bastools.tools.bt;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Callable;

import io.github.applecommander.applesingle.AppleSingle;
import org.applecommander.bastools.api.*;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Token.Type;
import org.applecommander.bastools.api.visitors.ByteVisitor;
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
public class Main implements Callable<Integer> {
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

	@Option(names = "--wrapper", description = "Wrap the Applesoft program (DOS 3.3).")
	private boolean wrapProgram;
	
	@Option(names = "-f", converter = OptimizationTypeConverter.class, split = ",", description = {
			"Enable specific optimizations.",
			"* @|green remove-empty-statements|@ - Strip out all '::'-like statements.",
			"* @|green remove-rem-statements|@ - Remove all REM statements.",
			"* @|green shorten-variable-names|@ - Ensure all variables are 1 or 2 characters long.",
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
			public void write(int b) {
				// Do nothing
			}
		});

    @Option(names = "--tokenizer", description = "Select tokenizer (modern, classic)")
    private String tokenizer = "modern";
	
	@Parameters(index = "0", description = "AppleSoft BASIC program to process.")
	private File sourceFile;
	
	public static void main(String[] args) {
		// The CLI unit test library throws an exception when 'System.exit' is called;
		// so we cannot have the 'System.exit' call in the try-catch block!
		int exitCode = 0;
		try {
			exitCode = new CommandLine(new Main()).execute(args);
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
			exitCode = 1;
		}
		System.exit(exitCode);
	}
	
	@Override
	public Integer call() throws IOException {
		if (checkParameters()) {
			Configuration.Builder builder = Configuration.builder()
					.maxLineLength(this.maxLineLength)
					.sourceFile(this.sourceFile)
					.startAddress(this.address);
			if (debugFlag) builder.debugStream(System.out);
			process(builder.build());
		}
		
		return 0;
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
	public void process(Configuration config) throws IOException {
		Queue<Token> tokens = switch (tokenizer) {
            case "modern" -> ModernTokenReader.tokenize(sourceFile);
            case "classic" -> ClassicTokenReader.tokenize(sourceFile);
            default -> throw new RuntimeException("Unknown tokenizer: " + tokenizer);
        };
		if (showTokens) {
			tokens.forEach(t -> System.out.printf("%s%s", t, t.type() == Type.EOL ? "\n" : ", "));
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
		byte[] wrapperData = new byte[0];
		if (wrapProgram) {
			Queue<Token> wrapperTokens = ModernTokenReader.tokenize(new ByteArrayInputStream(
					"10 POKE 103,24:POKE 104,8:RUN".getBytes()));
			Parser wrapperParser = new Parser(wrapperTokens);
			Program wrapperProgram = wrapperParser.parse();
			wrapperData = byteVisitor.dump(wrapperProgram);
		}

		byte[] programData = byteVisitor.dump(program);
		if (showLineAddresses) {
			byteVisitor.getLineAddresses().forEach((l,a) -> System.out.printf("%5d ... $%04x\n", l, a));
		}

		// Merge both programs together. Note that wrapperData may be a 0 byte array.
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.write(wrapperData);
		output.write(programData);
		output.flush();
		byte[] data = output.toByteArray();

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
