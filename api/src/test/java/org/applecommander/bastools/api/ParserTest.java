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
package org.applecommander.bastools.api;

import java.util.Queue;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Statement;
import org.junit.Assert;
import org.junit.Test;

import org.applecommander.bastools.api.model.Program;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.utils.TokenBuilder;

import static org.junit.Assert.assertEquals;

public class ParserTest {
    @Test
    public void testBlankLines() {
        Queue<Token> tokens = TokenBuilder.builder()
                .eol()  // Blank line before
                .number(10.0).ident("A").syntax('=').number(42.0).eol()
                .eol()  // Blank line after
                .tokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        Assert.assertNotNull(program);
    }

    @Test
    public void testMinusInData() {
        Queue<Token> tokens = TokenBuilder.builder()
                // 10 DATA -5
                .number(10.0).keyword(ApplesoftKeyword.DATA).keyword(ApplesoftKeyword.sub).number(5.0).eol()
                .tokens();
        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        assertEquals(1, program.lines.size());
        assertEquals(1, program.lines.getFirst().statements.size());
        Statement statement = program.lines.getFirst().statements.getFirst();
        assertEquals(3, statement.tokens.size());
        assertEquals(ApplesoftKeyword.DATA, statement.tokens.get(0).keyword());
        // In this case, the "-" is treated as text
        assertEquals("-", statement.tokens.get(1).text());
        assertEquals(Double.valueOf(5.0), statement.tokens.get(2).number());
    }

    @Test
    public void testMinusInExpression() {
        Queue<Token> tokens = TokenBuilder.builder()
                // 10 A=-5
                .number(10.0).ident("A").keyword(ApplesoftKeyword.eq).keyword(ApplesoftKeyword.sub).number(5.0).eol()
                .tokens();

        Parser parser = new Parser(tokens);
        Program program = parser.parse();
        assertEquals(1, program.lines.size());
        assertEquals(1, program.lines.getFirst().statements.size());
        Statement statement = program.lines.getFirst().statements.getFirst();
        assertEquals(4, statement.tokens.size());
        assertEquals(Token.Type.IDENT, statement.tokens.get(0).type());
        assertEquals(ApplesoftKeyword.eq, statement.tokens.get(1).keyword());
        // In this case, the "-" is an actual keyword
        assertEquals(ApplesoftKeyword.sub, statement.tokens.get(2).keyword());
        assertEquals(Double.valueOf(5.0), statement.tokens.get(3).number());
    }
}
