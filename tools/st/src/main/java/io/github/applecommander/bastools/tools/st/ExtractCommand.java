package io.github.applecommander.bastools.tools.st;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import io.github.applecommander.bastools.api.shapes.Shape;
import io.github.applecommander.bastools.api.shapes.ShapeExporter;
import io.github.applecommander.bastools.api.shapes.ShapeTable;
import io.github.applecommander.bastools.api.shapes.exporters.TextShapeExporter.BorderStrategy;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Visibility;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "extract", description = { "Extract shapes from shape table" },
		parameterListHeading = "%nParameters:%n",
		descriptionHeading = "%n",
		optionListHeading = "%nOptions:%n")
public class ExtractCommand implements Callable<Void> {
	@Option(names = { "-h", "--help" }, description = "Show help for subcommand", usageHelp = true)
	private boolean helpFlag;

    @Option(names = "--stdin", description = "Read from stdin")
    private boolean stdinFlag;

	@Option(names = "--stdout", description = "Write to stdout")
	private boolean stdoutFlag;

	@Option(names = { "-o", "--output" }, description = "Write to filename")
	private String filename;
	
    @Option(names = "--border", description = "Set border style (none, simple, box)", showDefaultValue = Visibility.ALWAYS)
    private String borderStyle = "simple";
    
    @Option(names = { "-w", "--width" }, description = "Set text width", showDefaultValue = Visibility.ALWAYS)
    private int textWidth = 80;
    
    @Option(names = "--shape", description = "Extract specific shape")
    private int shapeNum = 0;
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path file;

	private BorderStrategy borderStrategy;
	
	@Override
	public Void call() throws IOException {
	    validateArguments();
	    
	    ShapeTable shapeTable = stdinFlag ? ShapeTable.read(System.in) : ShapeTable.read(file);
	    
	    ShapeExporter exporter = ShapeExporter.text()
	                                          .borderStrategy(borderStrategy)
	                                          .maxWidth(textWidth)
	                                          .build();
	    
	    if (shapeNum > 0) {
	        if (shapeNum <= shapeTable.shapes.size()) {
	            Shape shape = shapeTable.shapes.get(shapeNum-1);
	            if (stdoutFlag) {
                    exporter.export(shape, System.out);
	            } else {
                    exporter.export(shape, Paths.get(filename));
	            }
	        } else {
	            throw new IOException("Invalid shape number");
	        }
        } else {
            if (stdoutFlag) {
                exporter.export(shapeTable, System.out);
            } else {
                exporter.export(shapeTable, Paths.get(filename));
            }
        }
	    
	    return null;
	}
	
	private void validateArguments() throws IOException {
        if (stdoutFlag && filename != null) {
            throw new IOException("Please choose one of stdout or output file");
        }
        if ((stdinFlag && file != null) || (!stdinFlag && file == null)) {
            throw new IOException("Please select ONE of stdin or file");
        }
        switch (borderStyle) {
        case "box":
            this.borderStrategy = BorderStrategy.BOX_DRAWING;
            break;
        case "simple":
            this.borderStrategy = BorderStrategy.ASCII_TEXT;
            break;
        case "none":
            this.borderStrategy = BorderStrategy.NONE;
            break;
        default:
            throw new IOException("Please select a valid border strategy");
        }
	}
}