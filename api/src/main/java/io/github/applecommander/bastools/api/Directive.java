package io.github.applecommander.bastools.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;
import io.github.applecommander.bastools.api.utils.Converters;

public abstract class Directive {
	protected Configuration config;
	protected OutputStream outputStream;
	protected List<Token> parameters = new ArrayList<>();

	protected Directive(Configuration config, OutputStream outputStream) {
		Objects.requireNonNull(config);
		Objects.requireNonNull(outputStream);
		this.config = config;
		this.outputStream = outputStream;
	}
	
	public void append(Token token) {
		// Skip the commas...
		if (token.type == Type.SYNTAX && ",".equals(token.text)) return;
		parameters.add(token);
	}
	protected Token require(Type... types) {
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
		return Converters.toInteger(t.text);
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
}
