package com.webcodepro.applecommander.util.applesoft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import com.webcodepro.applecommander.util.applesoft.Token.Type;

import io.github.applecommander.bastokenizer.Main.IntegerTypeConverter;

public abstract class Directive {
	private static Map<String,Class<? extends Directive>> directives = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);;
	static {
		Directive.directives.put("$embed", EmbeddedBinaryDirective.class);
	}
	
	private static IntegerTypeConverter integerConverter = new IntegerTypeConverter();
	
	protected OutputStream outputStream;
	protected List<Token> parameters = new ArrayList<>();

	public static Directive find(String text, OutputStream outputStream) {
		if (directives.containsKey(text)) {
			try {
				// Bypassing the constructor with arguments ... as that reduces code in the subclasses.
				Directive directive = directives.get(text).newInstance();
				directive.setOutputStream(outputStream);
				return directive;
			} catch (InstantiationException | IllegalAccessException e) {
				throw new IllegalArgumentException(String.format("Unable to construct directive '%s'", text), e);
			}
		}
		throw new IllegalArgumentException(String.format("Unable to find directive '%s'", text));
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}
	
	public void append(Token token) {
		// Skip the commas...
		if (token.type == Type.SYNTAX && ",".equals(token.text)) return;
		parameters.add(token);
	}
	private Token require(Type... types) {
		Token t = parameters.remove(0);
		boolean matches = false;
		for (Type type : types) {
			matches |= type == t.type;
		}
		if (!matches) {
			throw new IllegalArgumentException("Expecting a type of " + types);
		}
		return t;
	}
	protected String requiresString() {
		Token t = require(Type.STRING);
		return t.text;
	}
	protected int requiresInteger() {
		Token t = require(Type.NUMBER, Type.STRING);
		if (t.type == Type.NUMBER) {
			return t.number.intValue();
		}
		return integerConverter.convert(t.text);
	}
	
	protected void ldy(int value) throws IOException {
		outputStream.write(0xa0);
		outputStream.write(value);
	}
	protected void jmp(int address) throws IOException {
		outputStream.write(0x4c);
		outputStream.write(address & 0xff);
		outputStream.write(address >> 8);
	}
	protected void lda(int value) throws IOException {
		outputStream.write(0xa9);
		outputStream.write(value);
	}
	protected void sta(int address) throws IOException {
		if ((address & 0xff00) == 0) {
			outputStream.write(0x85);
			outputStream.write(address);
		} else {
			throw new RuntimeException("sta does not handle 16 bit addresses yet!");
		}
	}
	protected void setAddress(int value, int address) throws IOException {
		lda(value & 0xff);
		sta(address);
		lda(value >> 8);
		sta(address+1);
	}
	
	/** Write directive contents to output file. Note that address is adjusted for the line header already. */
	public abstract void writeBytes(int startAddress, Line line) throws IOException;
	
	public static class EmbeddedBinaryDirective extends Directive {
		@Override
		public void writeBytes(int startAddress, Line line) throws IOException {
			if (parameters.size() != 2) {
				throw new IllegalArgumentException("$embed requires a name and address parameter");
			}
			String filename = requiresString();
			int targetAddress = requiresInteger();
			
			Path path = Paths.get(filename);
			byte[] bin = Files.readAllBytes(path);
			
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
}
