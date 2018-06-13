package io.github.applecommander.bastools.api.directives;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Optional;

import io.github.applecommander.bastools.api.Configuration;
import io.github.applecommander.bastools.api.Directive;
import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Line;

public class EmbeddedBinaryDirective extends Directive {
	public EmbeddedBinaryDirective(Configuration config, OutputStream outputStream) {
		super(config, outputStream);
	}
	
	@Override
	public void writeBytes(int startAddress, Line line) throws IOException {
		if (parameters.size() != 2) {
			throw new IllegalArgumentException("$embed requires a name and address parameter");
		}
		String filename = requiresString();
		int targetAddress = requiresInteger();

		File file = new File(config.sourceFile.getParentFile(), filename);
		byte[] bin = Files.readAllBytes(file.toPath());
		
		Optional<Line> nextLine = line.nextLine();
		byte[] basicCode = nextLine.isPresent() 
				? callAndGoto(startAddress,nextLine.get()) 
				: callAndReturn(startAddress);
		
		final int moveLength = 8*3 + 2 + 3;	// LDA/STA, LDY, JMP.
		int embeddedStart = startAddress + basicCode.length + moveLength;
		int embeddedEnd = embeddedStart + bin.length;
		
		outputStream.write(basicCode);
		setAddress(embeddedStart, 0x3c);
		setAddress(embeddedEnd, 0x3e);
		setAddress(targetAddress, 0x42);
		ldy(0x00);
		jmp(0xfe2c);
		outputStream.write(bin);
	}
	// In program, "CALL <address>:GOTO line"
	private byte[] callAndGoto(int startAddress, Line line) throws IOException {
		// 3 for the tokens "CALL", ":", "GOTO", end of line (0x00)
		final int tokenCount = 3 + 1;
		int offset = Integer.toString(line.lineNumber).length() + tokenCount;
		offset += Integer.toString(startAddress).length();
		// Attempting to adjust if we bump from 4 digit address to a 5 digit address
		if (startAddress < 10000 && startAddress + offset >= 10000) offset += 1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(ApplesoftKeyword.CALL.code);
		os.write(Integer.toString(startAddress+offset).getBytes());
		os.write(':');
		os.write(ApplesoftKeyword.GOTO.code);
		os.write(Integer.toString(line.lineNumber).getBytes());
		os.write(0x00);
		return os.toByteArray();
	}
	// At end of program, just "CALL <address>:RETURN"
	private byte[] callAndReturn(int startAddress) throws IOException {
		// 3 for the tokens "CALL", ":", "RETURN", end of line (0x00)
		final int tokenCount = 3 + 1;
		int offset = tokenCount;
		offset += Integer.toString(startAddress).length();
		// Attempting to adjust if we bump from 4 digit address to a 5 digit address
		if (startAddress < 10000 && startAddress + offset >= 10000) offset += 1;
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(ApplesoftKeyword.CALL.code);
		os.write(Integer.toString(startAddress+offset).getBytes());
		os.write(':');
		os.write(ApplesoftKeyword.RETURN.code);
		os.write(0x00);
		return os.toByteArray();
	}
}
