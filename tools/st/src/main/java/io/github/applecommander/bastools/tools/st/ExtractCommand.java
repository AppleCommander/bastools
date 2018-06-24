package io.github.applecommander.bastools.tools.st;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

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

	@Option(names = { "-o", "--output" }, description = "Write output to file")
	private Path outputFile;
	
    @Option(names = "--border", description = "Set border style (none, simple, box)", showDefaultValue = Visibility.ALWAYS)
    private String borderStyle = "simple";
    
    @Option(names = "--format", description = "Select output format (text, source, png, gif, jpeg, bmp, wbmp)", showDefaultValue = Visibility.ALWAYS)
    private String outputFormat = "text";
    
    @Option(names = "--coding", description = "Select source style (bitmap, long, short)", showDefaultValue = Visibility.ALWAYS)
    private String codeStyle;
    
    @Option(names = "--skip-empty", description = "Skip empty shapes")
    private boolean skipEmptyShapesFlag = false;
    
    @Option(names = { "-w", "--width" }, description = "Set width (defaults: text=80, image=1024)")
    private int width = -1;

    @Option(names = "--shapes", description = "Extract specific shape(s); formats are '1' or '1-4' and can be combined with a comma",
            converter = IntegerRangeTypeConverter.class)
    private List<Integer> shapeNums = new ArrayList<>();
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path inputFile;

	private BorderStrategy borderStrategy;
	
	@Override
	public Void call() throws IOException {
	    ShapeExporter exporter = validateAndParseArguments();
	    
	    ShapeTable shapeTable = stdinFlag ? ShapeTable.read(System.in) : ShapeTable.read(inputFile);
	    
	    if (shapeNums.isEmpty()) {
            if (stdoutFlag) {
                exporter.export(shapeTable, System.out);
            } else {
                exporter.export(shapeTable, outputFile);
            }
        } else {
            List<Integer> outOfRange = shapeNums.stream()
                                                .filter(n -> n > shapeTable.shapes.size())
                                                .collect(Collectors.toList());
            if (!outOfRange.isEmpty()) {
                throw new IOException("Invalid shape numbers: " + outOfRange);
            }

            OutputStream outputStream = System.out;
            try {
                if (outputFile != null) {
                    outputStream = Files.newOutputStream(outputFile);
                }
                for (int shapeNum : shapeNums) {
                    Shape shape = shapeTable.shapes.get(shapeNum - 1);
                    exporter.export(shape, outputStream);
                }
            } finally {
                if (outputFile != null) {
                    outputStream.close();
                }
            }
        }
	    
	    return null;
	}
	
	private ShapeExporter validateAndParseArguments() throws IOException {
        if (stdoutFlag && outputFile != null) {
            throw new IOException("Please choose one of stdout or output file");
        }
        if ((stdinFlag && inputFile != null) || (!stdinFlag && inputFile == null)) {
            throw new IOException("Please select ONE of stdin or file");
        }
        
        // Assign defaults
        if (!stdoutFlag && outputFile == null) {
            outputFile = Paths.get("shapes.txt");
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
        
        ShapeExporter exporter = null;
        switch (outputFormat) {
        case "text":
            exporter = ShapeExporter.text()
                                    .borderStrategy(borderStrategy)
                                    .maxWidth(width == -1 ? 80 : width)
                                    .skipEmptyShapes(skipEmptyShapesFlag)
                                    .build();
            break;
        case "png":
        case "jpeg":
        case "gif":
        case "bmp":
        case "wbmp":
            exporter = ShapeExporter.image()
                                    .border(borderStrategy != BorderStrategy.NONE)
                                    .maxWidth(width == -1 ? 1024 : width)
                                    .imageFormat(outputFormat)
                                    .skipEmptyShapes(skipEmptyShapesFlag)
                                    .build();
            break;
        case "source":
            switch (codeStyle) {
            case "bitmap":
                exporter = ShapeExporter.source()
                                        .bitmap()
                                        .skipEmptyShapes(skipEmptyShapesFlag)
                                        .build();
                break;
            case "short":
                exporter = ShapeExporter.source()
                                        .shortCommands()
                                        .skipEmptyShapes(skipEmptyShapesFlag)
                                        .build();
                break;
            case "long":
                exporter = ShapeExporter.source()
                                        .longCommands()
                                        .skipEmptyShapes(skipEmptyShapesFlag)
                                        .build();
                break;
            default:
                throw new IOException("Please select a valid code style");
            }
            break;
        default:
            throw new IOException("Please select a valid output format");
        }
        return exporter;
	}
}