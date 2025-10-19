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
package io.github.applecommander.bastools.tools.st;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.webcodepro.applecommander.storage.DiskException;
import com.webcodepro.applecommander.storage.FileEntry;
import com.webcodepro.applecommander.storage.FormattedDisk;
import com.webcodepro.applecommander.storage.os.prodos.ProdosFormatDisk;
import com.webcodepro.applecommander.storage.physical.ByteArrayImageLayout;
import com.webcodepro.applecommander.storage.physical.ImageOrder;
import com.webcodepro.applecommander.storage.physical.ProdosOrder;

import io.github.applecommander.applesingle.AppleSingle;
import io.github.applecommander.applesingle.Utilities;
import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Parser;
import io.github.applecommander.bastools.api.TokenReader;
import io.github.applecommander.bastools.api.Visitors;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.shapes.BitmapShape;
import io.github.applecommander.bastools.api.shapes.Shape;
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
	
	@Option(names = "--demo-code", description = "Generate a ProDOS .po image with Applesoft BASIC code demoing the shape table")
	private boolean demoCodeFlag;

	@Option(names = { "-o", "--output" }, description = "Write output to file")
	private Path outputFile;
	
	@Parameters(arity = "0..1", description = "File to process")
	private Path inputFile;
	
	@Override
	public Void call() throws IOException, DiskException {
	    validateArguments();
	    
	    ShapeTable st = stdinFlag ? ShapeGenerator.generate(System.in) : ShapeGenerator.generate(inputFile);
	    
	    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
	    st.write(byteStream);

	    if (demoCodeFlag) {
	        byteStream.reset();
	        byteStream.write(generateDemoCode(st));
	    } else if (applesingleFlag) {
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
        
        if (demoCodeFlag && applesingleFlag) {
            System.err.println("Warning: Demo code and AppleSingle exclusive, ignoring AppleSingle request.");
            applesingleFlag = false;
        }

        // Assign defaults
        if (!stdoutFlag && outputFile == null) {
            outputFile = Paths.get("shape.out");
        }
	}
	
	private byte[] generateDemoCode(ShapeTable shapeTable) throws IOException, DiskException {
	    // Get shape metadata
        List<BitmapShape> blist = shapeTable.shapes.stream()
                                                   .map(Shape::toBitmap)
                                                   .toList();
        int width = blist.stream().mapToInt(BitmapShape::getWidth).max().getAsInt();
        int height = blist.stream().mapToInt(BitmapShape::getHeight).max().getAsInt();
        
        // Insert variables into program code
        String demoProgram = new String(Utilities.toByteArray(getClass().getResourceAsStream("/demo-template.bas")))
                .replace("$SOURCE$", stdinFlag ? "STDIN" : inputFile.toFile().getName().toUpperCase())
                .replace("$COUNT$", Integer.toString(shapeTable.shapes.size()))
                .replace("$WIDTH$", Integer.toString(width))
                .replace("$HEIGHT$", Integer.toString(height));

        // Generate Applesoft program data
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(demoProgram.getBytes());
        Configuration config = Configuration.builder().sourceFile(new File("FAKEFILE")).build();
        Queue<Token> tokens = TokenReader.tokenize(sourceStream);
        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        byte[] programBytes = Visitors.byteVisitor(config).dump(program);
        
        // Generate Shape table binary data
        ByteArrayOutputStream shapeTableStream = new ByteArrayOutputStream();
        shapeTable.write(shapeTableStream);
        byte[] shapeTableBytes = shapeTableStream.toByteArray();
        
        // Copy template into AppleCommander. Note that there doesn't appear to be a load from stream capability.
        byte[] templateBytes = Utilities.toByteArray(getClass().getResourceAsStream("/template.po"));
        ByteArrayImageLayout layout = new ByteArrayImageLayout(templateBytes.length);
        ImageOrder imageOrder = new ProdosOrder(layout);
        FormattedDisk[] disks = ProdosFormatDisk.create("DELETEME", "GONESOON", imageOrder);
        FormattedDisk template = disks[0];
        template.getDiskImageManager().setDiskImage(templateBytes);
        
        // Copy in BASIC code.
        FileEntry basicFile = template.createFile();
        basicFile.setFilename("STARTUP");
        basicFile.setFiletype("BAS");
        basicFile.setAddress(config.startAddress);
        basicFile.setFileData(programBytes);
        
        // Copy in shape table.
        FileEntry shapeFile = template.createFile();
        shapeFile.setFilename("SHAPES.BIN");
        shapeFile.setFiletype("BIN");
        shapeFile.setAddress(0x6000);
        shapeFile.setFileData(shapeTableBytes);
        
        return template.getDiskImageManager().getDiskImage();
	}
}