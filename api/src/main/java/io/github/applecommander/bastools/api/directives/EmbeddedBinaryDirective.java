package io.github.applecommander.bastools.api.directives;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Directive;
import io.github.applecommander.bastools.api.code.CodeBuilder;
import io.github.applecommander.bastools.api.code.CodeMark;
import io.github.applecommander.bastools.api.model.Line;

/**
 * Embed an binary file into a BASIC program.  See writeup in the README-TOKENIZER.md file.
 */
public class EmbeddedBinaryDirective extends Directive {
    public static final String NAME = "$embed";
    public static final String PARAM_FILE = "file";
    public static final String PARAM_MOVETO = "moveto";
    public static final String PARAM_VAR = "var";
    
	public EmbeddedBinaryDirective(Configuration config, OutputStream outputStream) {
		super(NAME, config, outputStream, PARAM_FILE, PARAM_MOVETO, PARAM_VAR);
	}
	
	@Override
	public void writeBytes(int startAddress, Line line) throws IOException {
		String filename = requiredStringExpression(PARAM_FILE, "$embed requires a 'name=<string>' parameter");
		Optional<Integer> targetAddress = optionalIntegerExpression(PARAM_MOVETO);
		Optional<String> variableName = optionalStringExpression(PARAM_VAR);
		
		validateSet(ONLY_ONE, "$embed requires either a 'var' assignment or a 'moveto' parameter", targetAddress, variableName);

		File file = new File(config.sourceFile.getParentFile(), filename);
		byte[] bin = Files.readAllBytes(file.toPath());

		CodeBuilder builder = new CodeBuilder();
		CodeMark moveStart = new CodeMark();
		CodeMark embeddedStart = new CodeMark();
		CodeMark embeddedEnd = new CodeMark();
		
		variableName.ifPresent(var -> {
		    builder.basic()
    	           .assign(resolve(var), embeddedStart)
    	           .endStatement();
		});
		
		targetAddress.ifPresent(address -> {
		    builder.basic()
		           .CALL(moveStart)
		           .endStatement();
		    
    		Optional<Line> nextLine = line.nextLine();
    		if (nextLine.isPresent()) {
    		    builder.basic()
		               .GOTO(nextLine.get().lineNumber);
    		} else {
    		    builder.basic()
        		       .RETURN();
    		}
		});

		builder.basic()
               .endLine();


		targetAddress.ifPresent(address -> {
			builder.set(moveStart)
		           .asm()
                   .setAddress(embeddedStart, 0x3c)
                   .setAddress(embeddedEnd, 0x3e)
                   .setAddress(address, 0x42)
                   .ldy(0x00)
                   .jmp(0xfe2c)
                   .end();
		});

		builder.set(embeddedStart)
	           .addBinary(bin)
	           .set(embeddedEnd);
		
		builder.generate(startAddress)
		       .writeTo(super.outputStream);
	}
}
