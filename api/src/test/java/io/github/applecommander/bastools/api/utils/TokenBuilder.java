package io.github.applecommander.bastools.api.utils;

import java.util.LinkedList;
import java.util.Queue;

import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Token;

public class TokenBuilder {
    private int lineNumber;
    private final Queue<Token> tokens = new LinkedList<Token>();
    
    public static TokenBuilder builder() {
        return new TokenBuilder();
    }
    
    public TokenBuilder eol() {
        add(Token.eol(lineNumber));
        lineNumber += 1;
        return this;
    }
    public TokenBuilder number(Double number) {
        return add(Token.number(lineNumber, number));
    }
    public TokenBuilder ident(String text) {
        return add(Token.ident(lineNumber, text));
    }
    public TokenBuilder comment(String text) {
        return add(Token.comment(lineNumber, text));
    }
    public TokenBuilder string(String text) {
        return add(Token.string(lineNumber, text));
    }
    public TokenBuilder keyword(ApplesoftKeyword keyword) {
        return add(Token.keyword(lineNumber, keyword));
    }
    public TokenBuilder syntax(int ch) {
        return add(Token.syntax(lineNumber, ch));
    }
    public TokenBuilder directive(String text) {
        return add(Token.directive(lineNumber, text));
    }
    private TokenBuilder add(Token token) {
        tokens.add(token);
        return this;
    }
    public Queue<Token> tokens() {
        return tokens;
    }
}
