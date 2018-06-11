package io.github.applecommander.bastokenizer.api.optimizations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import io.github.applecommander.bastokenizer.api.Configuration;
import io.github.applecommander.bastokenizer.api.Visitors;
import io.github.applecommander.bastokenizer.api.model.ApplesoftKeyword;
import io.github.applecommander.bastokenizer.api.model.Line;
import io.github.applecommander.bastokenizer.api.model.Program;
import io.github.applecommander.bastokenizer.api.model.Statement;
import io.github.applecommander.bastokenizer.api.model.Token;
import io.github.applecommander.bastokenizer.api.utils.VariableNameGenerator;
import io.github.applecommander.bastokenizer.api.visitors.VariableCollectorVisitor;

/** 
 * Find constants and extract to variables in order to have the number parsed only once.
 */
public class ExtractConstantValues extends BaseVisitor {
	/** These trigger the start of a replacement range.  Note the special logic for assignments. */
	public static List<ApplesoftKeyword> TARGET_STARTS = Arrays.asList(
			ApplesoftKeyword.FOR, ApplesoftKeyword.CALL, ApplesoftKeyword.PLOT, ApplesoftKeyword.HLIN, 
			ApplesoftKeyword.VLIN, ApplesoftKeyword.HCOLOR, ApplesoftKeyword.HPLOT, ApplesoftKeyword.DRAW, 
			ApplesoftKeyword.XDRAW, ApplesoftKeyword.HTAB, ApplesoftKeyword.SCALE, ApplesoftKeyword.COLOR, 
			ApplesoftKeyword.VTAB, ApplesoftKeyword.HIMEM, ApplesoftKeyword.LOMEM, ApplesoftKeyword.SPEED, 
			ApplesoftKeyword.LET, ApplesoftKeyword.IF, ApplesoftKeyword.ON, ApplesoftKeyword.WAIT, 
			ApplesoftKeyword.POKE);
	/** These trigger the end of a replacement range.  End of statement is always an end. */
	public static List<ApplesoftKeyword> TARGET_ENDS = Arrays.asList(
			ApplesoftKeyword.GOTO, ApplesoftKeyword.GOSUB, ApplesoftKeyword.THEN);
	
	// Map keyed by value (Double isn't a good key, using a String of the number) and pointing to replacement variable name
	private Map<String,String> map = new HashMap<>();
	
	private VariableNameGenerator variableGenerator = new VariableNameGenerator();
	private Set<String> existingVariables;
	private Function<Token,Token> consumer = this::nullTransformation;
	
	public ExtractConstantValues(Configuration config) {
		// ignored
	}
	
	public Token nullTransformation(Token token) {
		return token;
	}
	/** Collect a map of constant values and the new variable name to be used. */
	public Token numberToIdentTransformation(Token token) {
		String key = token.number.toString();
		// New entry, create it
		if (!map.containsKey(key)) {
			String varName = null;
			do {
				varName = variableGenerator.get()
						                   .orElseThrow(() -> new RuntimeException("Ran out of variable names to assign"));
			} while (existingVariables.contains(varName));
			map.put(key, varName);
		}
		// Existing (or NEW!) entry, swap to that variable.
		if (map.containsKey(key)) {
			return Token.ident(token.line, map.get(key));
		}
		return token;
	}
	
	@Override
	public Program visit(Program program) {
		VariableCollectorVisitor collector = Visitors.variableCollectorVisitor();
		program.accept(collector);
		this.existingVariables = collector.getVariableNames();
		
		program = super.visit(program);
		
		injectLine0(program);
		
		return program;
	}
	private void injectLine0(Program program) {
		Line line = generateLine0(program);
		// Bypass if there were no constants
		if (line.statements.isEmpty()) return;
		// setup a renumber of lines that interfere if we have any
		if (program.lines.get(0).lineNumber == 0) {
			// start with line #0 should become line #1
			super.reassignments.put(0, 1);
			// chase it to the end!
			program.lines.stream()
				   .map(Line::getLineNumber)
				   .filter(super.reassignments::containsValue)
				   .forEach(n -> { super.reassignments.put(n, n+1); });
		}
		program.lines.add(0, line);
	}
	private Line generateLine0(Program program) {
		Line line = new Line(0, program);
		map.entrySet().stream()
		   .sorted(Map.Entry.comparingByValue())
		   .map(this::toStatement)
		   .forEach(line.statements::add);
		return line;
	}
	private Statement toStatement(Map.Entry<String,String> variable) {
		Statement statement = new Statement();
		statement.tokens.add(Token.ident(-1, variable.getValue()));
		statement.tokens.add(Token.syntax(-1, '='));
		statement.tokens.add(Token.number(-1, Double.valueOf(variable.getKey())));
		return statement;
	}
	
	@Override
	public Statement visit(Statement statement) {
		try {
			if (!statement.tokens.isEmpty()) {
				int size = statement.tokens.size();
				Token t = statement.tokens.get(0);
				// Special logic for "A=5+1" while trying to skip constant forms of "A=1234" (don't replicate)
				if (t.type == Token.Type.IDENT && size > 3) {
					this.consumer = this::numberToIdentTransformation;
				}
				// Special logic for "LET A=5+1" while trying to skip constant forms of "LET A=1234" (don't replicate)
				if (t.type == Token.Type.KEYWORD && t.keyword == ApplesoftKeyword.LET && size > 4) {
					this.consumer = this::numberToIdentTransformation;
				}
			}
			return super.visit(statement);
		} finally {
			this.consumer = this::nullTransformation;
		}
	}
	
	@Override
	public Token visit(Token token) {
		switch (token.type) {
		case KEYWORD:
			if (TARGET_STARTS.contains(token.keyword)) {
				this.consumer = this::numberToIdentTransformation;
			} else if (TARGET_ENDS.contains(token.keyword)) {
				this.consumer = this::nullTransformation;
			}
			break;
		case NUMBER:
			return this.consumer.apply(token);
		default:
			break;
		}
		return super.visit(token);
	}
}
