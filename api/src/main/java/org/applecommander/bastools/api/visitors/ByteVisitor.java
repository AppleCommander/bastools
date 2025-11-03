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
package org.applecommander.bastools.api.visitors;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.TreeMap;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.Directive;
import org.applecommander.bastools.api.Directives;
import org.applecommander.bastools.api.Visitor;
import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Line;
import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Statement;
import org.applecommander.bastools.api.model.Token;

public class ByteVisitor implements Visitor {
	private final Stack<ByteArrayOutputStream> stack;
	private final Map<Integer,Integer> lineAddresses;
	private final Configuration config;
	private int address;
	private Directive currentDirective;
	
	public ByteVisitor(Configuration config) {
		this.config = config;
		this.address = config.startAddress;
		this.stack = new Stack<>();
		this.lineAddresses = new TreeMap<>();
	}
	
	/** A convenience method to invoke {@link Program#accept(Visitor)} and {@link #getBytes()}. */
	public byte[] dump(Program program) {
		program.accept(this);
		return getBytes();
	}
	
	/** A convenience method to get the length of a line. */
	public int length(Line line) {
		stack.push(new ByteArrayOutputStream());
		line.accept(this);
		return stack.pop().size();
	}
	
	public Map<Integer, Integer> getLineAddresses() {
		return lineAddresses;
	}
	
	public byte[] getBytes() {
		if (stack.size() != 1) {
			throw new RuntimeException("Error in processing internal BASIC model!");
		}
		return stack.peek().toByteArray();
	}
	
	@Override
	public Program visit(Program program) {
		stack.clear();
		stack.push(new ByteArrayOutputStream());
		program.lines.forEach(line -> line.accept(this));
		ByteArrayOutputStream os = stack.peek();
		os.write(0x00);
		os.write(0x00);
		this.address += 2;
		return program;
	}
	
	@Override
	public Line visit(Line line) {
		try {
			stack.push(new ByteArrayOutputStream());
			boolean first = true;
			for (Statement statement : line.statements) {
				if (currentDirective != null) {
					throw new RuntimeException("No statements are allowed after a directive!");
				}
				if (!first) {
					stack.peek().write(':');
				}
				first = false;
				statement.accept(this);
			}
			if (currentDirective != null) {
			    // Need to force the last set of parameters to be processed. Yeah, stinky. :-)
			    currentDirective.append(Token.eol(-1));
				currentDirective.writeBytes(this.address+4, line);
				currentDirective = null;
			}

			this.lineAddresses.put(line.lineNumber, this.address);
			byte[] content = stack.pop().toByteArray();
			int nextAddress = address + content.length + 5;
			ByteArrayOutputStream os = stack.peek();
			os.write(nextAddress);
			os.write(nextAddress >> 8);
			os.write(line.lineNumber);
			os.write(line.lineNumber >> 8);
			os.write(content);
			os.write(0x00);
			this.address = nextAddress;
			return line;
		} catch (IOException ex) {
			// Hiding the IOException as ByteArrayOutputStream does not throw it
			throw new RuntimeException(ex);
		}
	}

	@Override
	public Token visit(Token token) {
		if (currentDirective != null) {
			currentDirective.append(token);
			return token;
		}
		try {
			ByteArrayOutputStream os = stack.peek();
			switch (token.type()) {
			case COMMENT:
				os.write(ApplesoftKeyword.REM.code);
				os.write(token.text().getBytes());
				break;
			case EOL:
				os.write(0x00);
				break;
			case IDENT, DATA:
				os.write(token.text().getBytes());
				break;
			case KEYWORD:
				os.write(token.keyword().code);
				break;
			case DIRECTIVE:
				currentDirective = Directives.find(token.text(), config, os);
				break;
			case NUMBER:
				if (Math.rint(token.number()) == token.number()) {
					os.write(Integer.toString(token.number().intValue()).getBytes());
				} else {
					os.write(Double.toString(token.number()).getBytes());
				}
				break;
            case STRING:
				os.write('"');
				os.write(token.text().getBytes());
				os.write('"');
				break;
			case SYNTAX:
				Optional<ApplesoftKeyword> opt = ApplesoftKeyword.find(token.text());
				if (opt.isPresent()) {
					os.write(opt.get().code);
				} else {
					os.write(token.text().getBytes());
				}
				break;
			}
			return token;
		} catch (IOException ex) {
			// Hiding the IOException as ByteArrayOutputStream does not throw it
			throw new RuntimeException(ex);
		}
	}
}
