/*
 * bastools
 * Copyright (C) 2026  Robert Greene
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

import java.util.ArrayList;
import java.util.List;

/**
 * A test utility class to help building expected tokens.
 */
public class Tokens {
    public static Builder builder() {
        return new Builder(1);
    }

    public static class Builder {
        private final List<Token> tokens = new ArrayList<>();
        private final int lineNo;
        private Builder(int lineNo) {
            this.lineNo = lineNo;
        }
        public Builder number(String value) {
            tokens.add(Token.number(lineNo, Double.valueOf(value), value));
            return this;
        }
        public Builder data(String data) {
            tokens.add(Token.data(lineNo, data));
            return this;
        }
        public Builder string(String text) {
            tokens.add(Token.string(lineNo, text));
            return this;
        }
        public Builder ident(String name) {
            tokens.add(Token.ident(lineNo, name));
            return this;
        }
        public Builder keyword(ApplesoftKeyword keyword) {
            tokens.add(Token.keyword(lineNo, keyword));
            return this;
        }
        public Builder syntax(int ch) {
            tokens.add(Token.syntax(lineNo, ch));
            return this;
        }
        public Builder comment(String comment) {
            tokens.add(Token.comment(lineNo, comment));
            return this;
        }
        public Builder nextLine() {
            tokens.add(Token.eol(lineNo));
            Builder b = new Builder(lineNo + 1);
            b.tokens.addAll(this.tokens);
            return b;
        }
        public Token[] end() {
            tokens.add(Token.eol(lineNo));
            return tokens.toArray(new Token[0]);
        }
    }
}
