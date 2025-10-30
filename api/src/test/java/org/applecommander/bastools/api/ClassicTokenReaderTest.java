package org.applecommander.bastools.api;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Token;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Queue;

import static org.junit.Assert.*;

public class ClassicTokenReaderTest {
    @Test
    public void testSimple() throws IOException {
        testCode("10 TEXT:HOME",
            Token.number(1, 10.0, "10"),
            Token.keyword(1, ApplesoftKeyword.TEXT),
            Token.syntax(1, ':'),
            Token.keyword(1, ApplesoftKeyword.HOME),
            Token.eol(1));
    }

    @Test
    public void preserveNumbers49() throws IOException {
        testCode("""
                10 PRINT "MATHING"
                30 A = .4
                40 B = 0.6000
                50 C = -.250
                60 D = -0.70
                """,
            Token.number(1, 10.0, "10"),
            Token.keyword(1, ApplesoftKeyword.PRINT),
            Token.string(1, "\"MATHING\""),
            Token.eol(1),
            Token.number(2, 30.0, "30"),
            Token.ident(2, "A"),
            Token.syntax(4, '='),
            Token.number(2, 0.4, ".4"),
            Token.eol(2),
            Token.number(3, 40.0, "40"),
            Token.ident(3, "B"),
            Token.syntax(3, '='),
            Token.number(3, 0.6, "0.6000"),
            Token.eol(3),
            Token.number(4, 50.0, "50"),
            Token.ident(4, "C"),
            Token.syntax(4, '='),
            Token.keyword(4, ApplesoftKeyword.sub),
            Token.number(4, 0.25, ".250"),
            Token.eol(4),
            Token.number(5, 60.0, "60"),
            Token.ident(5, "D"),
            Token.syntax(5, '='),
            Token.keyword(5, ApplesoftKeyword.sub),
            Token.number(5, 0.7, "0.70"),
            Token.eol(5));
    }

    @Test
    public void preserveNumbers49b() throws IOException {
        testCode("100 CI = 11 * I1 + .4 / I8 - 16 * .50",
                Token.number(1, 100.0, "100"),
                Token.ident(1, "CI"),
                Token.syntax(1, '='),
                Token.number(1, 11.0, "11"),
                Token.keyword(1, ApplesoftKeyword.mul),
                Token.ident(1, "I1"),
                Token.keyword(1, ApplesoftKeyword.add),
                Token.number(1, 0.4, ".4"),
                Token.keyword(1, ApplesoftKeyword.div),
                Token.ident(1, "I8"),
                Token.keyword(1, ApplesoftKeyword.sub),
                Token.number(1, 16.0, "16"),
                Token.keyword(1, ApplesoftKeyword.mul),
                Token.number(1, 0.5, ".50"),
                Token.eol(1));
    }

    public void testCode(String code, Token... expectedTokens) throws IOException {
        String expectedCode = tokensToString(expectedTokens);
        Queue<Token> actualTokens = ClassicTokenReader.tokenize(new StringReader(code));
        String actualCode = tokensToString(actualTokens.toArray(new Token[0]));
        assertEquals(expectedCode, actualCode);
    }

    public String tokensToString(Token... tokens) {
        StringBuilder sb = new StringBuilder();
        boolean spaceNeeded = false;
        for (Token token : tokens) {
            if (spaceNeeded) sb.append(' ');
            sb.append(token.asString());
            spaceNeeded = token.type() != Token.Type.EOL;
        }
        return sb.toString();
    }
}
