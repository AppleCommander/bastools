package io.github.applecommander.bastools.api.directives;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.Optional;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Directive;
import io.github.applecommander.bastools.api.code.BasicBuilder;
import io.github.applecommander.bastools.api.code.CodeBuilder;
import io.github.applecommander.bastools.api.code.CodeMark;
import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.shapes.Shape;
import io.github.applecommander.bastools.api.shapes.ShapeGenerator;
import io.github.applecommander.bastools.api.shapes.ShapeTable;

/**
 * Embed an Applesoft shape table into a BASIC program.  See writeup in the README-TOKENIZER.md file.
 */
public class EmbeddedShapeTable extends Directive {
    public static final String NAME = "$shape";
    public static final String PARAM_SRC = "src";
    public static final String PARAM_LABEL = "label";
    public static final String VALUE_VARIABLE = "variable";
    public static final String PARAM_BIN = "bin";
    public static final String PARAM_POKE = "poke";
    public static final String PARAM_ASSIGN = "assign";
    public static final String PARAM_INIT = "init";
    public static final String PARAM_ADDRESS = "address";
    
    public EmbeddedShapeTable(Configuration config, OutputStream outputStream) {
        super(NAME, config, outputStream, PARAM_SRC, PARAM_LABEL, PARAM_BIN, PARAM_POKE, 
                PARAM_ASSIGN, PARAM_INIT, PARAM_ADDRESS);
    }
    
    /**
     * Parse the given parameters, generating code and embedding shape table as directed.
     */
    @Override
    public void writeBytes(int startAddress, Line line) throws IOException {
        Optional<String> src = optionalStringExpression(PARAM_SRC);
        Optional<String> label = optionalStringExpression(PARAM_LABEL);
        Optional<MapExpression> assign = optionalMapExpression(PARAM_ASSIGN);
        Optional<String> bin = optionalStringExpression(PARAM_BIN);
        boolean poke = defaultBooleanExpression(PARAM_POKE, true);
        boolean init = defaultBooleanExpression(PARAM_INIT, true);
        Optional<String> address = optionalStringExpression(PARAM_ADDRESS);

        // Validation
        validateSet(ONLY_ONE, "Please include a 'src' or a 'bin' as part $shape directive, but not both", src, bin);
        validateSet(ZERO_OR_ONE, "Cannot specify both 'label' and 'assign' in $shape directive", label, assign);
        bin.ifPresent(x -> validateSet(ZERO, "'bin' does not support 'label' or 'assign'", label, assign));

        // Load in specified data file
        Optional<byte[]> binData = bin.map(this::readBin);
        Optional<ShapeTable> shapeTable = src.map(this::readSrc);

        // Setup code builders
        CodeMark shapeTableStart = new CodeMark();
        CodeBuilder builder = new CodeBuilder();
        BasicBuilder basic = builder.basic();
        
        // Setup common code
        if (poke) basic.POKEW(232, shapeTableStart).endStatement();
        if (init) basic.ROT(0).endStatement().SCALE(1).endStatement();
        address.ifPresent(var -> basic.assign(var, shapeTableStart).endStatement());
        
        // Inject src options
        assign.ifPresent(expr -> setupVariables(expr, basic, shapeTable));
        label.ifPresent(opt -> setupLabels(opt, basic, shapeTable));

        // We need to terminate a binary embedded line with some mechanism of skipping the binary content.
        Optional<Line> nextLineOpt = line.nextLine();
        nextLineOpt.ifPresent(nextLine -> basic.GOTO(nextLine.lineNumber));
        if (!nextLineOpt.isPresent()) basic.RETURN();

        // End line and inject binary content
        basic.endLine().set(shapeTableStart);
        binData.ifPresent(builder::addBinary);
        shapeTable.map(this::mapShapeTableToBin).ifPresent(builder::addBinary);
        
        builder.generate(startAddress).writeTo(this.outputStream);
    }

    public void setupVariables(MapExpression expr, BasicBuilder basic, Optional<ShapeTable> shapeTableOptional) {
        ShapeTable st = shapeTableOptional.orElseThrow(() -> new RuntimeException("ShapeTable source not supplied"));
        expr.entrySet().forEach(e -> {
            String label = e.getValue().toSimpleExpression()
                                       .map(SimpleExpression::asString)
                                       .orElseThrow(() -> new RuntimeException(
                                               String.format("Unexpected format of asignments for variable '%s'", e.getKey())));
            basic.assign(e.getKey(), st.findPositionByLabel(label)).endStatement();
        });
    }
    
    public void setupLabels(String labelOption, BasicBuilder basic, Optional<ShapeTable> shapeTableOptional) {
        if (!"variable".equalsIgnoreCase(labelOption)) {
            throw new RuntimeException(String.format("Unexpected label option of '%s'", labelOption));
        }
        ShapeTable st = shapeTableOptional.orElseThrow(() -> new RuntimeException("ShapeTable source not supplied"));
        for (int i=0; i<st.shapes.size(); i++) {
            Shape s = st.shapes.get(i);
            basic.assign(s.getLabel(), i+1);
        }
    }

    public byte[] readBin(String filename) {
        try {
            File file = new File(config.sourceFile.getParentFile(), filename);
            return Files.readAllBytes(file.toPath());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    public ShapeTable readSrc(String filename) {
        try {
            File file = new File(config.sourceFile.getParentFile(), filename);
            return ShapeGenerator.generate(file);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
    public byte[] mapShapeTableToBin(ShapeTable shapeTable) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            shapeTable.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
