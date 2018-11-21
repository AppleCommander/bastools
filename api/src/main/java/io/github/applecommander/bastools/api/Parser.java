package io.github.applecommander.bastools.api;

import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Program;
import io.github.applecommander.bastools.api.model.Statement;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;

/** 
 * The Parser will read a series of Tokens and build a Program.
 * Note that this is not a compiler and does not "understand" the program. 
 */
public class Parser {
	private final Queue<Token> tokens;
	
	public Parser(Queue<Token> tokens) {
		Objects.requireNonNull(tokens);
		this.tokens = tokens;
	}
	
	public Program parse() {
		Program program = new Program();
		while (!tokens.isEmpty()) {
		    readLine(program).ifPresent(program.lines::add);
		}
		return program;
	}
	
	public Optional<Line> readLine(Program program) {
	    return expectNumber().map(lineNumber -> {
            Line line = new Line(lineNumber, program);
            while (!tokens.isEmpty() && tokens.peek().type != Type.EOL) {
                Statement statement = readStatement();
                if (statement != null) {
                    line.statements.add(statement);
                } else {
                    break;
                }
            }
            if (!tokens.isEmpty() && tokens.peek().type == Type.EOL) {
                tokens.remove();    // Skip that EOL
            }
            return line;
	    });
	}
	
	public Statement readStatement() {
		Statement statement = new Statement();
		while (!tokens.isEmpty()) {
			if (tokens.peek().type == Type.EOL) break;
			Token t = tokens.remove();
			if (t.type == Type.SYNTAX && ":".equals(t.text)) break;
			statement.tokens.add(t);
		}
		return statement;
	}
	
	public Optional<Integer> expectNumber() {
		Token c = tokens.remove();
		while (c.type == Type.EOL) {
		    return Optional.empty();
		}
		if (c.type != Type.NUMBER) {
			throw new RuntimeException("Expected a number in line #" + c.line);
		}
		return Optional.of(c.number.intValue());
	}
}
