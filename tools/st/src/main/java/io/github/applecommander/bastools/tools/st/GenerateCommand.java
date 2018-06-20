package io.github.applecommander.bastools.tools.st;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.bastools.api.shapes.ShapeGenerator;
import io.github.applecommander.bastools.api.shapes.ShapeTable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "generate", description = { "Generate a shape table from source code" },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class GenerateCommand implements Callable<Void> {
    public static final int BIN = 0x06;

	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

    @Option(names = "--stdin", description = "Read from stdin")
    private boolean stdinFlag;

	@Option(names = "--stdout", description = "Write to stdout")
	private boolean stdoutFlag;
	
	@Option(names = "--single", description = "Write to AppleSingle file (requires address, defaults to 0x6000)")
	private boolean applesingleFlag;
	
	@Option(names = "--address", description = "Address for AppleSingle file", showDefaultValue = Visibility.ALWAYS)
	private int address = 0x6000;
	
	@Option(names = "--name", description = "Filename assign in AppleSingle file", showDefaultValue = Visibility.ALWAYS)
	private String realName = "SHAPES.BIN";

	@Option(names = { "-o", "--output" }, description = "Write output to file")
	private Path outputFile;
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path inputFile;
	
	@Override
	public Void call() throws IOException {
	    validateArguments();
	    
	    ShapeTable st = stdinFlag ? ShapeGenerator.generate(System.in) : ShapeGenerator.generate(inputFile);
	    
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    st.write(byteStream);

	    if (applesingleFlag) {
	        AppleSingle applesingle = AppleSingle.builder()
            	                                 .realName(realName)
            	                                 .dataFork(byteStream.toByteArray())
            	                                 .auxType(address)
            	                                 .fileType(BIN)
            	                                 .build();
	        byteStream.reset();
	        applesingle.save(byteStream);
	    }

	    if (stdoutFlag) {
	        System.out.write(byteStream.toByteArray());
	    } else {
	        try (OutputStream outputStream = Files.newOutputStream(outputFile)) {
	            outputStream.write(byteStream.toByteArray());
	        }
	    }
	    
	    return null;
	}
	
	private void validateArguments() throws IOException {
        if (stdoutFlag && outputFile != null) {
            throw new IOException("Please choose one of stdout or output file");
        }
        if ((stdinFlag && inputFile != null) || (!stdinFlag && inputFile == null)) {
            throw new IOException("Please select ONE of stdin or file");
        }

        // Assign defaults
        if (!stdoutFlag && outputFile == null) {
            outputFile = Paths.get("shape.out");
        }
	}
}