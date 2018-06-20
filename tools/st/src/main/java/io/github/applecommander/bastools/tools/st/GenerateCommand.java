package io.github.applecommander.bastools.tools.st;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import io.github.applecommander.bastools.api.shapes.ShapeGenerator;
import io.github.applecommander.bastools.api.shapes.ShapeTable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "generate", description = { "Generate a shape table from source code" },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class GenerateCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

    @Option(names = "--stdin", description = "Read from stdin")
    private boolean stdinFlag;

	@Option(names = "--stdout", description = "Write to stdout")
	private boolean stdoutFlag;

	@Option(names = { "-o", "--output" }, description = "Write output to file")
	private Path outputFile;
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path inputFile;

	@Override
	public Void call() throws IOException {
	    validateArguments();
	    
	    ShapeTable st = stdinFlag ? ShapeGenerator.generate(System.in) : ShapeGenerator.generate(inputFile); 
	    
	    if (stdoutFlag) {
	        st.write(System.out);
	    } else {
	        st.write(outputFile);
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