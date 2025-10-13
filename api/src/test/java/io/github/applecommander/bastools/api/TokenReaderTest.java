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
