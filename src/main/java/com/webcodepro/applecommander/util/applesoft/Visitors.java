package com.webcodepro.applecommander.util.applesoft;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;

import com.webcodepro.applecommander.util.applesoft.Token.Type;

/**
 * This class presents all of the common Visitor implementations via builder patterns.
 * The number is currently small enough that all the builders and visitors are defined 
 * in this one class.
 *  
 * @author rob
 */
public class Visitors {
	public static PrintBuilder printBuilder() {
		return new PrintBuilder();
	}
	public static class PrintBuilder {
		private PrintStream printStream = System.out;
		private Function<PrintBuilder,Visitor> creator = PrintVisitor::new;
		
		public PrintBuilder printStream(PrintStream printStream) {
			Objects.requireNonNull(printStream);
			this.printStream = printStream;
			return this;
		}
		public PrintBuilder prettyPrint(boolean flag) {
			creator = flag ? PrettyPrintVisitor::new : PrintVisitor::new;
			return this;
		}
		public PrintBuilder prettyPrint() {
			creator = PrettyPrintVisitor::new;
			return this;
		}
		public PrintBuilder print() {
			creator = PrintVisitor::new;
			return this;
		}
		
		public Visitor build() {
			return creator.apply(this);
		}
	}
	
	public static ByteVisitor byteVisitor(int address) {
		return new ByteVisitor(address);
	}
	
	/** Rewrite the Program tree with the line number reassignments given. */
	public static ReassignmentVisitor reassignVisitor(Map<Integer,Integer> reassignments) {
		return new ReassignmentVisitor(reassignments);
	}

	private static class PrettyPrintVisitor implements Visitor {
		private PrintStream printStream;
		
		private PrettyPrintVisitor(PrintBuilder builder) {
			this.printStream = builder.printStream;
		}
		
		@Override
		public Line visit(Line line) {
			boolean first = true;
			for (Statement statement : line.statements) {
				if (first) {
					first = false;
					printStream.printf("%5d ", line.lineNumber);
				} else {
					printStream.printf("%5s ", ":");
				}
				statement.accept(this);
				printStream.println();
			}
			return line;
		}
		@Override
		public Token visit(Token token) {
			switch (token.type) {
			case EOL:
				printStream.print("<EOL>");
				break;
			case COMMENT:
				printStream.printf(" REM %s", token.text);
				break;
			case STRING:
				printStream.printf("\"%s\"", token.text);
				break;
			case KEYWORD:
				printStream.printf(" %s ", token.keyword.text);
				break;
			case IDENT:
			case SYNTAX:
				printStream.print(token.text);
				break;
			case NUMBER:
				if (Math.rint(token.number) == token.number) {
					printStream.print(token.number.intValue());
				} else {
					printStream.print(token.number);
				}
				break;
			}
			return token;
		}
	}
	
	private static class PrintVisitor implements Visitor {
		private PrintStream printStream;
		
		private PrintVisitor(PrintBuilder builder) {
			this.printStream = builder.printStream;
		}
		
		@Override
		public Line visit(Line line) {
			printStream.printf("%d ", line.lineNumber);
			boolean first = true;
			for (Statement statement : line.statements) {
				if (first) {
					first = false;
				} else {
					printStream.printf(":");
				}
				statement.accept(this);
			}
			printStream.println();
			return line;
		}
		@Override
		public Token visit(Token token) {
			switch (token.type) {
			case EOL:
				printStream.print("<EOL>");
				break;
			case COMMENT:
				printStream.printf("REM %s", token.text);
				break;
			case STRING:
				printStream.printf("\"%s\"", token.text);
				break;
			case KEYWORD:
				printStream.printf(" %s ", token.keyword.text);
				break;
			case IDENT:
			case SYNTAX:
				printStream.print(token.text);
				break;
			case NUMBER:
				if (Math.rint(token.number) == token.number) {
					printStream.print(token.number.intValue());
				} else {
					printStream.print(token.number);
				}
				break;
			}
			return token;
		}
	}
	
	public static class ByteVisitor implements Visitor {
		private Stack<ByteArrayOutputStream> stack;
		private int address;
		
		private ByteVisitor(int address) {
			this.address = address;
			this.stack = new Stack<>();
		}
		
		/** A convenience method to invoke {@link Program#accept(Visitor)} and {@link #getBytes()}. */
		public byte[] dump(Program program) {
			program.accept(this);
			return getBytes();
		}
		
		public byte[] getBytes() {
			if (stack.size() != 1) {
				throw new RuntimeException("Error in processing internal BASIC model!");
			}
			return stack.peek().toByteArray();
		}
		
		@Override
		public Program visit(Program program) {
			if (stack.size() != 0) {
				throw new RuntimeException("Please do not reuse this ByteVisitor as that is an unsafe operation.");
			}
			stack.push(new ByteArrayOutputStream());
			program.lines.forEach(line -> line.accept(this));
			ByteArrayOutputStream os = stack.peek();
			os.write(0x00);
			os.write(0x00);
			return program;
		}
		
		@Override
		public Line visit(Line line) {
			try {
				stack.push(new ByteArrayOutputStream());
				boolean first = true;
				for (Statement statement : line.statements) {
					if (!first) {
						stack.peek().write(':');
					} else {
						first = false;
					}
					statement.accept(this);
				}
				
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
			try {
				ByteArrayOutputStream os = stack.peek();
				switch (token.type) {
				case COMMENT:
					os.write(ApplesoftKeyword.REM.code);
					os.write(token.text.getBytes());
					break;
				case EOL:
					os.write(0x00);
					break;
				case IDENT:
					os.write(token.text.getBytes());
					break;
				case KEYWORD:
					os.write(token.keyword.code);
					break;
				case NUMBER:
					if (Math.rint(token.number) == token.number) {
						os.write(Integer.toString(token.number.intValue()).getBytes());
					} else {
						os.write(Double.toString(token.number).getBytes());
					}
					break;
				case STRING:
					os.write('"');
					os.write(token.text.getBytes());
					os.write('"');
					break;
				case SYNTAX:
					Optional<ApplesoftKeyword> opt = ApplesoftKeyword.find(token.text);
					if (opt.isPresent()) {
						os.write(opt.get().code);
					} else {
						os.write(token.text.getBytes());
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

	/** This is a mildly rewritable Visitor. */
	private static class ReassignmentVisitor implements Visitor {
		private Map<Integer,Integer> reassignments;
		
		private ReassignmentVisitor(Map<Integer,Integer> reassignments) {
			this.reassignments = reassignments;
		}
		
		@Override
		public Program visit(Program program) {
			Program newProgram = new Program();
			program.lines.forEach(l -> {
				Line line = l.accept(this);
				newProgram.lines.add(line);
			});
			return newProgram;
		}
		@Override
		public Line visit(Line line) {
			Line newLine = new Line(line.lineNumber);
			line.statements.forEach(s -> {
				Statement statement = s.accept(this);
				newLine.statements.add(statement);
			});
			return newLine;
		}
		/**
		 * We saw a trigger, reassign any numbers that follow.
		 * 
		 * Trigger cases:
		 * - GOSUB n
		 * - GOTO n
		 * - IF ... THEN n
		 * - LIST n [ ,m ]
		 * - ON x GOTO n, m, ...
		 * - ON x GOSUB n, m, ...
		 * - ONERR GOTO n
		 * - RUN n
		 */
		@Override
		public Statement visit(Statement statement) {
			boolean next = false;
			Statement newStatement = new Statement();
			for (Token t : statement.tokens) {
				Token newToken = t;
				if (next) {
					if (t.type == Type.NUMBER && reassignments.containsKey(t.number.intValue())) {
						newToken = Token.number(t.line, reassignments.get(t.number.intValue()).doubleValue());
					}
				} else {
					next = t.keyword == ApplesoftKeyword.GOSUB || t.keyword == ApplesoftKeyword.GOTO 
						|| t.keyword == ApplesoftKeyword.THEN || t.keyword == ApplesoftKeyword.RUN
						|| t.keyword == ApplesoftKeyword.LIST;
				}
				newStatement.tokens.add(newToken);
			}
			return newStatement;
		}
	}
}
