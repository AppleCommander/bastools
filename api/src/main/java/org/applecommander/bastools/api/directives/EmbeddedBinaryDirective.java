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
package org.applecommander.bastools.api.directives;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Directive;
import org.applecommander.bastools.api.code.CodeBuilder;
import org.applecommander.bastools.api.code.CodeMark;
import org.applecommander.bastools.api.model.Line;

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
