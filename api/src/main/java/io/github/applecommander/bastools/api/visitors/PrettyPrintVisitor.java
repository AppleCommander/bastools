package io.github.applecommander.bastools.api.visitors;

import java.io.PrintStream;

import io.github.applecommander.bastools.api.Visitor;
import io.github.applecommander.bastools.api.Visitors.PrintBuilder;
import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;

public class PrettyPrintVisitor implements Visitor {
	private final PrintStream printStream;
	
	public PrettyPrintVisitor(PrintBuilder builder) {
		this.printStream = builder.getPrintStream();
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
		case DIRECTIVE:
			printStream.printf("%s ", token.text);
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
