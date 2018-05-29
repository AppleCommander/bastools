package io.github.applecommander.bastokenizer.api.model;

import io.github.applecommander.bastokenizer.api.Visitor;

/**
 * A Token in the classic compiler sense, in that this represents a component of the application.
 * 
 * @author rob
 */
public class Token {
	public final int line;
	public final Type type;
	public final ApplesoftKeyword keyword;
	public final Double number;
	public final String text;
	
	private Token(int line, Type type, ApplesoftKeyword keyword, Double number, String text) {
		this.line = line;
		this.type = type;
		this.keyword = keyword;
		this.number = number;
		this.text = text;
	}
	public Token accept(Visitor t) {
		return t.visit(this);
	}
	@Override
	public String toString() {
		switch (type) {
		case EOL:
			return type.toString();
		case KEYWORD:
			return keyword.toString();
		case NUMBER:
			return String.format("%s(%f)", type, number);
		default:
			return String.format("%s(%s)", type, text);
		}
	}
	
	public static Token eol(int line) {
		return new Token(line, Type.EOL, null, null, null);
	}
	public static Token number(int line, Double number) {
		return new Token(line, Type.NUMBER, null, number, null);
	}
	public static Token ident(int line, String text) {
		return new Token(line, Type.IDENT, null, null, text.toUpperCase());
	}
	public static Token comment(int line, String text) {
		return new Token(line, Type.COMMENT, null, null, text);
	}
	public static Token string(int line, String text) {
		return new Token(line, Type.STRING, null, null, text);
	}
	public static Token keyword(int line, ApplesoftKeyword keyword) {
		// Note that the text component is useful to have for parsing, so we replicate it...
		return new Token(line, Type.KEYWORD, keyword, null, keyword.text);
	}
	public static Token syntax(int line, int ch) {
		return new Token(line, Type.SYNTAX, null, null, String.format("%c", ch));
	}
	public static Token directive(int line, String text) {
		return new Token(line, Type.DIRECTIVE, null, null, text);
	}
	
	public static enum Type {
		EOL, NUMBER, IDENT, COMMENT, STRING, KEYWORD, SYNTAX, DIRECTIVE
	}
}