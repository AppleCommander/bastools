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
package org.applecommander.bastools.api.model;

import org.applecommander.bastools.api.Visitor;

import java.util.Objects;

/**
 * A Token in the classic compiler sense, in that this represents a component of the application.
 *
 * @author rob
 */
public record Token(int line, Type type, ApplesoftKeyword keyword, Double number, String text) {
    public Token accept(Visitor t) {
        return t.visit(this);
    }

    @Override
    public String toString() {
        return switch (type) {
            case EOL -> type.toString();
            case KEYWORD -> keyword.toString();
            case NUMBER -> String.format("%s(%f)", type, number);
            default -> String.format("%s(%s)", type, text);
        };
    }

    public String asString() {
        return switch (type) {
            case EOL -> "\n";
            case NUMBER -> {
                if (text != null) {
                    yield text;
                } else {
                    yield number.toString();
                }
            }
            default -> text;
        };
    }

    public static Token eol(int line) {
        return new Token(line, Type.EOL, null, null, null);
    }
    public static Token number(int line, Double number) {
        return new Token(line, Type.NUMBER, null, number, null);
    }
    public static Token number(int line, Double number, String text) {
        return new Token(line, Type.NUMBER, null, number, text);
    }
    public static Token ident(int line, String text) {
        return new Token(line, Type.IDENT, null, null, text.toUpperCase());
    }
    public static Token comment(int line, String text) {
        return new Token(line, Type.COMMENT, null, null, text);
    }
    public static Token data(int line, String data) {
        return new Token(line, Type.DATA, null, null, data);
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

    public enum Type {
        EOL, NUMBER, IDENT, COMMENT, DATA, STRING, KEYWORD, SYNTAX, DIRECTIVE
    }
}