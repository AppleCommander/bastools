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
package io.github.applecommander.bastools.api;

import io.github.applecommander.bastools.api.model.ApplesoftKeyword;
import io.github.applecommander.bastools.api.model.Token;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Queue;

import static org.junit.Assert.*;

public class TokenReaderTest {
    @Test
    public void testCanRead() throws IOException {
        Queue<Token> tokens = TokenReader.tokenize("../samples/destroyer.bas");
        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
    }

    @Test
    public void testReadPR() throws IOException {
        Queue<Token> tokens = TokenReader.tokenize(new ByteArrayInputStream("40 PR = 6".getBytes()));
        assertEquals(Token.number(1, 40.0), tokens.remove());
        assertEquals(Token.ident(1, "PR"), tokens.remove());
        assertEquals(Token.keyword(1, ApplesoftKeyword.eq), tokens.remove());
        assertEquals(Token.number(1, 6.0), tokens.remove());
        assertTrue(tokens.isEmpty());
    }

    @Test
    public void testReadPRnum() throws IOException {
        Queue<Token> tokens = TokenReader.tokenize(new ByteArrayInputStream("10 PR#6".getBytes()));
        assertEquals(Token.number(1, 10.0), tokens.remove());
        assertEquals(Token.keyword(1, ApplesoftKeyword.PR), tokens.remove());
        assertEquals(Token.number(1, 6.0), tokens.remove());
        assertTrue(tokens.isEmpty());
    }
}
