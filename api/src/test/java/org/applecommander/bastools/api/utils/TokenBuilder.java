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
package org.applecommander.bastools.api.utils;

import java.util.LinkedList;
import java.util.Queue;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Token;

public class TokenBuilder {
    private int lineNumber;
    private final Queue<Token> tokens = new LinkedList<>();
    
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
