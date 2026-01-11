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
package org.applecommander.bastools.api.optimizations;

import org.applecommander.bastools.api.Configuration;
import org.applecommander.bastools.api.model.Token;

public class ShortenNumbers extends BaseVisitor {
    public ShortenNumbers(Configuration config) {
        if (!config.preserveNumbers) {
            System.err.println("Warning: number preservation should be enabled for shorten numbers optimization.");
        }
    }

    @Override
    public Token visit(Token token) {
        if (token.type() == Token.Type.NUMBER) {
            String text = Double.toString(token.number());
            if (Math.rint(token.number()) == token.number()) {
                text = Integer.toString(token.number().intValue());
            } else {
                while (text.startsWith("0") && text.length() > 1) {
                    text = text.substring(1);
                }
            }
            return Token.number(token.line(), token.number(), text);
        }
        return token;
    }
}
