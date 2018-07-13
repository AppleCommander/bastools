package io.github.applecommander.bastools.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Predicate;

import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Line;
import io.github.applecommander.bastools.api.model.Token;
import io.github.applecommander.bastools.api.model.Token.Type;
import io.github.applecommander.bastools.api.utils.Converters;

public abstract class Directive {
    private String directiveName;
	protected Configuration config;
	protected OutputStream outputStream;
	private List<Token> paramTokens = new ArrayList<>();
	private Map<String,Expression> parameters = new TreeMap<>(String::compareToIgnoreCase);
	private Set<String> parameterNames;

	protected Directive(String directiveName, Configuration config, OutputStream outputStream, String... parameterNames) {
	    Objects.requireNonNull(directiveName);
		Objects.requireNonNull(config);
		Objects.requireNonNull(outputStream);
		this.directiveName = directiveName;
		this.config = config;
		this.outputStream = outputStream;
		this.parameterNames = new TreeSet<>(String::compareToIgnoreCase);
		this.parameterNames.addAll(Arrays.asList(parameterNames));
	}
	
	public Optional<Expression> optionalExpression(String paramName) {
	    return Optional.ofNullable(parameters.get(paramName));
	}
//	public Expression requiredExpression(String paramName, String errorMessage) {
//	    return optionalExpression(paramName).orElseThrow(() -> new RuntimeException(errorMessage));
//	}
    public Optional<Integer> optionalIntegerExpression(String paramName) {
        return optionalExpression(paramName)
                   .flatMap(Expression::toSimpleExpression)
                   .map(SimpleExpression::asInteger);
    }
    public Integer requiredIntegerExpression(String paramName, String errorMessage) {
        return optionalIntegerExpression(paramName).orElseThrow(() -> new RuntimeException(errorMessage));
    }
    public Optional<String> optionalStringExpression(String paramName) {
        return optionalExpression(paramName)
                .flatMap(Expression::toSimpleExpression)
                .map(SimpleExpression::asString);
    }
    public boolean defaultBooleanExpression(String paramName, boolean defaultValue) {
        return optionalExpression(paramName)
                .flatMap(Expression::toSimpleExpression)
                .map(SimpleExpression::asBoolean)
                .orElse(defaultValue);
    }
    public String requiredStringExpression(String paramName, String errorMessage) {
        return optionalStringExpression(paramName).orElseThrow(() -> new RuntimeException(errorMessage));
    }
    public Optional<MapExpression> optionalMapExpression(String paramName) {
        return optionalExpression(paramName)
                .flatMap(Expression::toMapExpression);
    }

    public static final Predicate<Integer> ONLY_ONE = (n) -> n == 1;
    public static final Predicate<Integer> ZERO_OR_ONE = (n) -> n <= 1;
    public static final Predicate<Integer> ZERO = (n) -> n == 0;
    /** Validate a set of optionals with the given validator. If it fails, throw an exception with the message. */
    public void validateSet(Predicate<Integer> validator, String message, Optional<?>... opts) {
        int count = 0;
        for (Optional<?> opt : opts) {
            if (opt.isPresent()) count += 1;
        }
        if (!validator.test(count)) {
            throw new RuntimeException(message);
        }
    }

    /** 
     * Append directive tokens. Note that this MUST be terminated by a termination token 
     * (probably EOL) to prevent loss of information. 
     */
	public void append(Token token) {
	    if (token.type == Type.EOL || (token.type == Type.SYNTAX && ",".equals(token.text))) {
	        String name = requireIdentToken();
	        if (!parameterNames.contains(name)) {
	            String message = String.format("Parameter '%s' is invalid for %s directive", name, directiveName);
	            throw new RuntimeException(message);
	        }
	        requireSyntaxToken("=");
	        Expression expr = buildExpression();
	        parameters.put(name, expr);
	    } else {
	        paramTokens.add(token);
	    }
	}
	private Expression buildExpression() {
	    Token t = paramTokens.get(0);
	    if ("(".equals(t.text)) {
	        requireSyntaxToken("(");
	        Expression expr = buildMapExpression();
	        requireSyntaxToken(")");
	        return expr;
	    } else {
	        return buildSimpleExpression();
	    }
	}
	private Expression buildSimpleExpression() {
	    Token t = paramTokens.remove(0);
	    return new SimpleExpression(t.asString());
	}
	private Expression buildMapExpression() {
	    MapExpression mapex = new MapExpression();
	    boolean more = true;
	    while (more) {
	        String key = requireIdentToken();
	        requireSyntaxToken("=");
	        Expression expr = buildExpression();
	        mapex.expressions.put(key, expr);
	        more = checkSyntaxToken(",");
	        if (more) {
	            // Still need to consume it
	            requireSyntaxToken(",");
	        }
	    }
	    return mapex;
	}
	
	private Token requireToken(Type... types) {
		Token t = paramTokens.remove(0);
		boolean matches = false;
		for (Type type : types) {
			matches |= type == t.type;
		}
		if (!matches) {
		    String message = String.format("Expecting a token type of %s but found %s instead", 
		            Arrays.asList(types), t.type);
			throw new IllegalArgumentException(message);
		}
		return t;
	}
	private String requireIdentToken() {
		Token t = requireToken(Type.IDENT, Type.KEYWORD);
		return t.text;
	}
	private void requireSyntaxToken(String syntax) {
	    try {
    	    Type tokenType = ApplesoftKeyword.find(syntax).map(t -> Type.KEYWORD).orElse(Type.SYNTAX);
    	    Token token = requireToken(tokenType);
    	    if (!syntax.equals(token.text)) {
    	        String message = String.format("Expecting '%s' but found '%s' instead", syntax, token.text);
    	        throw new RuntimeException(message);
    	    }
	    } catch (IllegalArgumentException ex) {
	        throw new RuntimeException(String.format("Failed when token of '%s' was required", syntax));
	    }
	}
	private boolean checkSyntaxToken(String syntax) {
        Type tokenType = ApplesoftKeyword.find(syntax).map(t -> Type.KEYWORD).orElse(Type.SYNTAX);
        Token token = paramTokens.get(0);
        return tokenType == token.type && syntax.equals(token.text);
	}
	
	/** Write directive contents to output file. Note that address is adjusted for the line header already. */
	public abstract void writeBytes(int startAddress, Line line) throws IOException;
	
	public static class Variable {
	    public final String name;
	    public final Expression expr;
	    
	    private Variable(String name, Expression expr) {
	        this.name = name;
	        this.expr = expr;
	    }
	}
	public interface Expression {
	    public Optional<SimpleExpression> toSimpleExpression();
	    public Optional<MapExpression> toMapExpression();
	}
	public static class SimpleExpression implements Expression {
	    private final String value;
	    public SimpleExpression(String value) {
	        this.value = value;
	    }
	    public String asString() {
	        return value;
	    }
	    public Boolean asBoolean() {
	        return Converters.toBoolean(value);
	    }
	    public Integer asInteger() {
	        return Converters.toInteger(value);
	    }
        public Optional<SimpleExpression> toSimpleExpression() {
            return Optional.of(this);
        }
        public Optional<MapExpression> toMapExpression() {
            return Optional.empty();
        }
	}
	public static class MapExpression implements Expression {
	    private final Map<String,Expression> expressions = new HashMap<>();
	    public Optional<Expression> get(String key) {
	        return Optional.ofNullable(expressions.get(key));
	    }
	    public Set<Map.Entry<String,Expression>> entrySet() {
	        return expressions.entrySet();
	    }
        public Optional<SimpleExpression> toSimpleExpression() {
            return Optional.empty();
        }
        public Optional<MapExpression> toMapExpression() {
            return Optional.of(this);
        }
	}
}
