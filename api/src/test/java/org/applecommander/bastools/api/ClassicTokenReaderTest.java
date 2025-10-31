package org.applecommander.bastools.api;

import org.applecommander.bastools.api.model.ApplesoftKeyword;
import org.applecommander.bastools.api.model.Token;
import org.applecommander.bastools.api.model.Tokens;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Queue;

import static org.junit.Assert.*;

public class ClassicTokenReaderTest {
    @Test
    public void testSimple() throws IOException {
        testCode("10 TEXT:HOME",
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.TEXT)
                    .syntax(':')
                    .keyword(ApplesoftKeyword.HOME)
                .end());
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
            Tokens.builder()
                .number("10")
                    .keyword(ApplesoftKeyword.PRINT)
                    .string("\"MATHING\"")
                    .nextLine()
                .number("30")
                    .ident("A")
                    .syntax('=')
                    .number(".4")
                    .nextLine()
                .number("40")
                    .ident("B")
                    .syntax('=')
                    .number("0.6000")
                    .nextLine()
                .number("50")
                    .ident("C")
                    .syntax('=')
                    .keyword(ApplesoftKeyword.sub)
                    .number(".250")
                    .nextLine()
                .number("60")
                    .ident("D")
                    .syntax('=')
                    .keyword(ApplesoftKeyword.sub)
                    .number("0.70")
                .end());
    }

    @Test
    public void preserveNumbers49b() throws IOException {
        testCode("100 CI = 11 * I1 + .4 / I8 - 16 * .50",
                Tokens.builder()
                    .number("100")
                        .ident("CI")
                        .syntax('=')
                        .number("11")
                        .keyword(ApplesoftKeyword.mul)
                        .ident("I1")
                        .keyword(ApplesoftKeyword.add)
                        .number(".4")
                        .keyword(ApplesoftKeyword.div)
                        .ident("I8")
                        .keyword(ApplesoftKeyword.sub)
                        .number("16")
                        .keyword(ApplesoftKeyword.mul)
                        .number(".50")
                    .end());
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
